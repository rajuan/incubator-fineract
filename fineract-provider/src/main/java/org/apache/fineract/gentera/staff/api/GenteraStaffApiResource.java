/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.gentera.staff.api;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Path("/gentera/staff/{staffId}/groups")
@Component
@Scope("singleton")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class GenteraStaffApiResource {
    private static final Logger logger = LoggerFactory.getLogger(GenteraStaffApiResource.class);

    private final JdbcTemplate jdbcTemplate;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;

    @Autowired
    public GenteraStaffApiResource(final RoutingDataSource dataSource,
                                   final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
                                   final CalendarReadPlatformService calendarReadPlatformService,
                                   final StaffReadPlatformService staffReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
    }

    @GET
    public String staffGroups(@PathParam("staffId") final Long staffId) {
        List<Map<String, Object>> groups = new ArrayList<>();

        for(Long groupId : getGroups(staffId)) {
            CalendarData calendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(groupId, CalendarEntityType.GROUPS.getValue());
            Collection<LocalDate> recurringDates;
            if(calendar!=null) {
                recurringDates = this.calendarReadPlatformService.generateRecurringDates(calendar, false, LocalDate.now().plusMonths(1)); // NOTE: a month should be enough
                if(recurringDates!=null && !recurringDates.isEmpty()) {
                    LocalDate nextMeeting = recurringDates.iterator().next();

                    List<Map<String, Object>> schedules = getSchedule(groupId, nextMeeting);

                    if(schedules!=null && !schedules.isEmpty()) {
                        Map<String, Object> s = schedules.get(0);
                        s.put("id", groupId);
                        s.put("loanCycle", getGroupLoanCycle(groupId));
                        groups.add(s);
                    } else {
                        logger.warn("No schedule found for group: {}", groupId);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("staff", staffReadPlatformService.retrieveStaff(staffId));
        result.put("groups", groups);

        return this.toApiJsonSerializer.serialize(result);
    }

    private List<Long> getGroups(Long staffId) {
        String sql = "select " +
                "g.id, " +
                "from m_loan l, m_group g, m_group_client gc, m_client, m_loan_repayment_schedule lrs, m_loan_repayment_schedule_history lrsh " +
                "where " +
                "l.group_id = g.id and gc.group_id = g.id and gc.client_id = c.id and lrs.loan_id = l.id and lrsh.loan_id = l.id and lrsh.duedate = lrs.duedate and " +
                "g.staff_id = ? and l.loan_type_enum=3 and l.closedon_date is null and lrs.principal_writtenoff_derived is null " +
                "group by g.id ";

        return jdbcTemplate.queryForList(sql, Long.class, new Object[]{staffId});
    }

    private List<Map<String, Object>> getSchedule(Long groupId, LocalDate date) {
        String sql = "select " +
                "l.group_id, " +
                "count(l.id) as num_loans, " +
                "sum(lrs.principal_amount+lrs.interest_amount) as due_amount, " +
                "sum(lrsh.principal_amount+lrsh.interest_amount) as orig_due_amount, " +
                "sum((lrs.principal_amount+lrs.interest_amount)-(lrsh.principal_amount+lrsh.interest_amount)) as overpaid_amount, " +
                "lrs.duedate " +
                "from m_loan l, m_loan_repayment_schedule lrs, m_loan_repayment_schedule_history lrsh " +
                "where  " +
                "lrs.loan_id = l.id and lrsh.loan_id = l.id and lrsh.duedate = lrs.duedate and lrs.duedate = ? and " +
                "l.group_id = ? and l.loan_type_enum=3 and l.closedon_date is null and lrs.principal_writtenoff_derived is null " +
                "group by l.group_id, lrs.duedate " +
                "order by lrs.duedate";

        List<Map<String, Object>> schedule = jdbcTemplate.query(sql, new LoanScheduleMapper(), new Object[]{groupId, date.toString("yyyy-MM-dd")});
        List<Map<String, Object>> transactions = getTransactions(groupId);

        for(Map<String, Object> s : schedule) {
            s.put("transaction_amount", BigDecimal.ZERO);
            for(Map<String, Object> t : transactions) {
                LocalDate scheduleDate = (LocalDate)s.get("duedate_alt");
                LocalDate transactionDate = (LocalDate)t.get("transaction_date");

                if(transactionDate.equals(scheduleDate)) {
                    s.put("transaction_amount", t.get("amount"));
                    BigDecimal dueAmount = (BigDecimal)s.get("due_amount");
                    BigDecimal transactionAmount = (BigDecimal)t.get("amount");
                    s.put("due_amount", dueAmount.subtract(transactionAmount));
                    break;
                }
            }
        }

        return schedule;
    }

    private List<Map<String, Object>> getTransactions(Long groupId) {
        String sql = "select " +
                "lt.transaction_date, " +
                "sum(lt.amount) as amount " +
                "from m_loan l " +
                "left outer join m_loan_transaction lt on lt.loan_id = l.id " +
                "where l.group_id = ? and l.loan_type_enum=3 and l.closedon_date is null and l.writtenoffon_date is null and lt.is_reversed = false and lt.transaction_type_enum = 2 " +
                "group by lt.transaction_date " +
                "order by lt.transaction_date";

        return jdbcTemplate.query(sql, new LoanTransactionScheduleMapper(), new Object[]{groupId});
    }

    private List<Map<String, Object>> getScheduleByStaff(Long staffId) {
        String sql = "select " +
                "g.id, " +
                "g.display_name, " +
                "count(l.id) as num_loans, " +
                "sum(lrs.principal_amount+lrs.interest_amount) as due_amount, " +
                "sum(lrsh.principal_amount+lrsh.interest_amount) as orig_due_amount, " +
                "sum((lrs.principal_amount+lrs.interest_amount)-(lrsh.principal_amount+lrsh.interest_amount)) as overpaid_amount, " +
                "lrs.duedate " +
                "from m_loan l, m_group g, m_group_client gc, m_client, m_loan_repayment_schedule lrs, m_loan_repayment_schedule_history lrsh " +
                "where " +
                "l.group_id = g.id and gc.group_id = g.id and gc.client_id = c.id and lrs.loan_id = l.id and lrsh.loan_id = l.id and lrsh.duedate = lrs.duedate and " +
                "g.staff_id = ? and l.loan_type_enum=3 and l.closedon_date is null and lrs.principal_writtenoff_derived is null " +
                "group by g.id, lrs.duedate " +
                "order by lrs.duedate";

        return jdbcTemplate.query(sql, new LoanScheduleMapper(), new Object[]{staffId});
    }

    private Integer getGroupLoanCycle(Long groupId) {
        String sql = "select " +
                "max(lc.loan_cycle) as loan_cycle " +
                "from " +
                "m_loan l, loan_cycle lc " +
                "where " +
                "l.client_id = lc.client_id and l.group_id = ? " +
                "group by " +
                "l.group_id " +
                "order by " +
                "l.disbursedon_date desc";

        return jdbcTemplate.queryForObject(sql, Integer.class, new Object[]{groupId});
    }

    private static final class LoanTransactionScheduleMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final BigDecimal amount = rs.getBigDecimal("amount");
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transaction_date");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("amount", amount);
            schedule.put("transaction_date", transactionDate);

            return schedule;
        }
    }

    private static final class LoanScheduleMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            final Long numLoans = rs.getLong("num_loans");
            final BigDecimal dueAmount = rs.getBigDecimal("due_amount");
            final BigDecimal origDueAmount = rs.getBigDecimal("orig_due_amount");
            // final BigDecimal overpaidAmount = rs.getBigDecimal("overpaid_amount");
            final LocalDate dueDateAlt = JdbcSupport.getLocalDate(rs, "duedate");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("numLoans", numLoans);
            schedule.put("dueAmunt", dueAmount.compareTo(origDueAmount) == -1 ? dueAmount : origDueAmount);
            schedule.put("origDueAmount", origDueAmount);
            // schedule.put("overpaidAmount", overpaidAmount.signum() == -1 ? BigDecimal.ZERO : overpaidAmount);
            schedule.put("duedate", rs.getDate("duedate"));
            schedule.put("duedateAlt", dueDateAlt);

            return schedule;
        }
    }
}