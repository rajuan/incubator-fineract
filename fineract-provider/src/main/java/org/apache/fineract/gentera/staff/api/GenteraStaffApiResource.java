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

import org.apache.commons.collections.map.HashedMap;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
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

import javax.swing.text.StyledEditorKit;
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
        List<Map<String, Object>> schedules = getSchedule(staffId);

        List<Map<String, Object>> groups = new ArrayList<>();

        Object currentGroup = null;

        List<Map<String, Object>> tmp = new ArrayList<>();

        for(Map<String, Object> s : schedules) {
            if(s!=null  &&
                    !s.isEmpty()) {
                if(!s.get("groupId").equals(currentGroup) &&
                        !tmp.isEmpty()) {
                    Map<String, Object> t = nextMeeting((Long)tmp.get(0).get("groupId"), tmp);
                    t.put("loanCycle", getGroupLoanCycle((Long)t.get("groupId")));
                    groups.add(t);
                    logger.warn(">>> Selected group: {}", t.get("groupId"));
                    tmp = new ArrayList<>();
                }
                logger.warn("Adding group: {}", s.get("groupId"));
                tmp.add(s);
                currentGroup = s.get("groupId");
            }
        }

        Map<String, Object> staffResult = new HashMap<>();
        staffResult.put("staff", staffReadPlatformService.retrieveStaff(staffId));
        staffResult.put("groups", groups);

        return this.toApiJsonSerializer.serialize(staffResult);
    }

    private Map<String, Object> nextMeeting(Long groupId, List<Map<String, Object>> schedules) {
        LocalDate now = LocalDate.now();

        for(Map<String, Object> s : schedules) {
            LocalDate duedate = (LocalDate)s.get("duedateAlt");
            BigDecimal dueAmount = (BigDecimal)s.get("dueAmount");

            List<Map<String, Object>> transactions = getTransactions(groupId);
            for(Map<String, Object> t : transactions) {
                LocalDate paymentdate = (LocalDate)s.get("paymentdate");
                LocalDate transactionDate = (LocalDate)t.get("transaction_date");

                logger.warn("Comparing dates: {} <-> {}", paymentdate, transactionDate);

                if(transactionDate.equals(paymentdate)) {
                    logger.warn("Has transaction amount: {}", t.get("amount"));
                    BigDecimal transactionAmount = (BigDecimal)t.get("amount");
                    dueAmount = dueAmount.subtract(transactionAmount);
                    s.put("transactionAmount", transactionAmount);
                    s.put("dueAmount", dueAmount);
                    logger.warn("Has due amount: {}", dueAmount.subtract(transactionAmount));
                    break;
                } else {
                    logger.warn("Transaction skipped: {}", transactionDate);
                }
            }

            if(now.equals(duedate)) {
                logger.warn(">>> WAS SAME DATE: {} {}", dueAmount, duedate);
                return s;
            } else if(dueAmount!=null && dueAmount.compareTo(BigDecimal.ZERO)>0) {
                logger.warn(">>> WAS NON ZERO: {} {}", dueAmount, duedate);
                return s;
            } else if(dueAmount!=null && duedate.isAfter(now)) {
                logger.warn(">>> WAS AFTER DATE: {} {}", dueAmount, duedate);
                return s;
            }
        }

        return Collections.emptyMap();
    }

    private List<Map<String, Object>> getSchedule(Long staffId) {
        String sql = "select " +
                "l.group_id, " +
                "count(l.id) as num_loans, " +
                "sum(lrs.principal_amount+lrs.interest_amount) as due_amount, " +
                "sum(lrsh.principal_amount+lrsh.interest_amount) as orig_due_amount, " +
                "sum((lrs.principal_amount+lrs.interest_amount)-(lrsh.principal_amount+lrsh.interest_amount)) as overpaid_amount, " +
                "lrs.duedate, " +
                "lrs.duedate as paymentdate " +
                "from m_loan l, m_group g, m_loan_repayment_schedule lrs, m_loan_repayment_schedule_history lrsh " +
                "where " +
                "lrs.loan_id = l.id and lrsh.loan_id = l.id and g.id = l.group_id and lrsh.installment = lrs.installment and " +
                "g.staff_id = ? and l.loan_type_enum=3 and l.closedon_date is null and lrs.principal_writtenoff_derived is null " +
                "group by l.group_id, lrs.duedate " +
                "order by g.id, lrs.duedate";

        return jdbcTemplate.query(sql, new LoanScheduleMapper(), new Object[]{staffId});
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

            final Long groupId = rs.getLong("group_id");
            final Long numLoans = rs.getLong("num_loans");
            final BigDecimal dueAmount = rs.getBigDecimal("due_amount");
            final BigDecimal origDueAmount = rs.getBigDecimal("orig_due_amount");
            // final BigDecimal overpaidAmount = rs.getBigDecimal("overpaid_amount");
            final LocalDate dueDateAlt = JdbcSupport.getLocalDate(rs, "duedate");
            final LocalDate paymentdate = JdbcSupport.getLocalDate(rs, "paymentdate");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("groupId", groupId);
            schedule.put("numLoans", numLoans);
            schedule.put("dueAmount", dueAmount.compareTo(origDueAmount) == -1 ? dueAmount : origDueAmount);
            schedule.put("origDueAmount", origDueAmount);
            schedule.put("duedate", rs.getDate("duedate"));
            schedule.put("duedateAlt", dueDateAlt);
            schedule.put("paymentdate", paymentdate);

            return schedule;
        }
    }
}