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
package org.apache.fineract.gentera.transactions.api;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/gentera/groups/{groupId}/transactions")
@Component
@Scope("singleton")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class GenteraLoanTransactionsApiResource {
    private static final Logger logger = LoggerFactory.getLogger(GenteraLoanTransactionsApiResource.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadPlatformService meetingReadPlatformService;

    @Autowired
    public GenteraLoanTransactionsApiResource(final RoutingDataSource dataSource,
                                              final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
                                              final ApiRequestParameterHelper apiRequestParameterHelper,
                                              final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
                                              final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                              final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
                                              final ClientReadPlatformService clientReadPlatformService,
                                              final GroupReadPlatformService groupReadPlatformService,
                                              final CalendarReadPlatformService calendarReadPlatformService,
                                              final MeetingReadPlatformService meetingReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.meetingReadPlatformService = meetingReadPlatformService;
    }

    @GET
    public String groupMeetingTransactions(@PathParam("groupId") final Long groupId, @QueryParam("date") final String date) {
        GroupGeneralData group = groupReadPlatformService.retrieveOne(groupId);
        CalendarData calendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(groupId, CalendarEntityType.GROUPS.getValue());
        Collection<LocalDate> recurringDates = null;
        if(calendar!=null) {
            recurringDates = this.calendarReadPlatformService.generateRecurringDates(calendar, false, LocalDate.now().plusMonths(1)); // NOTE: a month should be enough
        }
        Collection<MeetingData> meetings = meetingReadPlatformService.retrieveMeetingsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), 100);

        Map<String, Object> result = new HashMap<>();
        result.put("group", group);
        result.put("meetings", meetings);
        result.put("transactions", getTransactions(groupId, date));
        result.put("schedule", getSchedule(groupId));
        if(recurringDates!=null && !recurringDates.isEmpty()) {
            result.put("nextMeeting", recurringDates.iterator().next());
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private List<Map<String, Object>> getTransactions(Long groupId, String date) {
        String sql = "select distinct " +
                "gc.group_id, " +
                "gc.client_id, " +
                "cl.firstname, " +
                "cl.firstname, " +
                "cl.lastname, " +
                "cl.external_id, " +
                "lrs.duedate, " +
                "ai.additional_family_name, " +
                "ai.account_number, " +
                "l.id as orig_loan_id, " +
                "l.currency_code, " +
                "count(lt.id) as num_transactions, " +
                "lrs.principal_amount + lrs.interest_amount as due_amount, " +
                "sum(lt.amount) as transaction_amount, " +
                "sum(lt.principal_portion_derived) as principal_portion_derived, " +
                "sum(lt.interest_portion_derived) as interest_portion_derived " +
                "from m_client cl  " +
                "left outer join addition_information ai on cl.id = ai.client_id  " +
                "left outer join m_group_roles gr on cl.id = gr.client_id  " +
                "left outer join m_group_client gc on cl.id = gc.client_id  " +
                "left outer join m_loan l on cl.id = l.client_id  " +
                "left outer join m_loan_repayment_schedule_history lrs on l.id = lrs.loan_id  " +
                "left outer join m_loan_transaction lt on lt.loan_id = l.id and (lt.transaction_type_enum = 2 or lt.transaction_type_enum is null) and (lt.is_reversed = false or lt.is_reversed is null)  " +
                "where  " +
                "(gr.group_id = gc.group_id or gr.group_id is null) and gc.group_id = ? and lrs.duedate = ? and (lt.transaction_date = ? or lt.transaction_date is null) and l.closedon_date is null and l.writtenoffon_date is null  " +
                "group by  " +
                "gc.group_id, " +
                "gc.client_id, " +
                "cl.firstname, " +
                "cl.firstname, " +
                "cl.lastname, " +
                "cl.external_id, " +
                "lrs.duedate, " +
                "ai.additional_family_name, " +
                "ai.account_number, " +
                "l.id";

        return jdbcTemplate.query(sql, new LoanTransactionMapper(getClientRoles(groupId)), new Object[]{groupId, date, date});
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

    private List<Map<String, Object>> getSchedule(Long groupId) {
        String sql = "select " +
                "l.group_id, " +
                "count(l.id) as num_loans, " +
                "sum(lrs.principal_amount+lrs.interest_amount) as due_amount, " +
                "sum(lrsh.principal_amount+lrsh.interest_amount) as orig_due_amount, " +
                "sum((lrs.principal_amount+lrs.interest_amount)-(lrsh.principal_amount+lrsh.interest_amount)) as overpaid_amount, " +
                "lrs.duedate " +
                "from m_loan l, m_loan_repayment_schedule lrs, m_loan_repayment_schedule_history lrsh " +
                "where  " +
                "lrs.loan_id = l.id and lrsh.loan_id = l.id and lrsh.duedate = lrs.duedate and " +
                "l.group_id = ? and l.loan_type_enum=3 and l.closedon_date is null and lrs.principal_writtenoff_derived is null " +
                "group by l.group_id, lrs.duedate " +
                "order by lrs.duedate";

        List<Map<String, Object>> schedule = jdbcTemplate.query(sql, new LoanScheduleMapper(), new Object[]{groupId});
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

    private Map<Long, String> getClientRoles(Long groupId) {
        String sql = "select distinct " +
                "cl.id,  " +
                "cv.code_value " +
                "from m_client cl  " +
                "join addition_information ai on cl.id = ai.client_id  " +
                "join m_group_roles gr on cl.id = gr.client_id  " +
                "join m_code_value cv on cv.code_id = gr.role_cv_id and cv.is_active = true  " +
                "where " +
                "cv.code_value = \"Leader\" and gr.group_id = ? " +
                "order by cl.id, cv.code_value ";

        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql, new Object[]{groupId});

        Map<Long, String> result = new HashMap<>();

        for(Map<String, Object> role : roles) {
            result.put((Long)role.get("id"), (String)role.get("code_value"));
        }

        return result;
    }

    private static final class LoanScheduleMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            final Long numLoans = rs.getLong("num_loans");
            final BigDecimal dueAmount = rs.getBigDecimal("due_amount");
            final BigDecimal origDueAmount = rs.getBigDecimal("orig_due_amount");
            final BigDecimal overpaidAmount = rs.getBigDecimal("overpaid_amount");
            final LocalDate dueDateAlt = JdbcSupport.getLocalDate(rs, "duedate");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("num_loans", numLoans);
            schedule.put("due_amount", dueAmount.compareTo(origDueAmount) == -1 ? dueAmount : origDueAmount);
            // schedule.put("orig_due_amount", origDueAmount);
            // schedule.put("overpaid_amount", overpaidAmount.signum() == -1 ? BigDecimal.ZERO : overpaidAmount);
            schedule.put("duedate", rs.getDate("duedate"));
            schedule.put("duedate_alt", dueDateAlt);

            return schedule;
        }
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

    private static final class LoanTransactionMapper implements RowMapper<Map<String, Object>> {
        private Map<Long, String> clientRoles;

        public LoanTransactionMapper(Map<Long, String> clientRoles) {
            this.clientRoles = clientRoles;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final CurrencyData currencyData = null; // TODO: remove; currency_code is enough; check with Ricardo
            final Long clientId = rs.getLong("client_id");
            final Long loanId = rs.getLong("orig_loan_id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String additionalFamilyName = rs.getString("additional_family_name");
            final String accountNumber = rs.getString("account_number");
            final String externalId = rs.getString("external_id");
            final Long numTransactions = rs.getLong("num_transactions");
            final String currencyCode = rs.getString("currency_code");
            final BigDecimal transactionAmount = rs.getBigDecimal("transaction_amount");
            final BigDecimal dueAmount = rs.getBigDecimal("due_amount");
            final BigDecimal principalPortionDerived = rs.getBigDecimal("principal_portion_derived");
            final BigDecimal interestPortionDerived = rs.getBigDecimal("interest_portion_derived");

            Map<String, Object> transaction = new HashMap<>();

            transaction.put("loanId", loanId);
            transaction.put("clientId", clientId);
            transaction.put("firstname", firstname);
            transaction.put("lastname", lastname);
            transaction.put("additionalFamilyName", additionalFamilyName);
            transaction.put("accountNumber", accountNumber);
            transaction.put("externalId", externalId);
            transaction.put("role", clientRoles.get(clientId));
            transaction.put("numTransactions", numTransactions);
            transaction.put("currencyCode", currencyCode);
            transaction.put("origDueAmount", dueAmount==null ? BigDecimal.ZERO : dueAmount);
            transaction.put("dueAmount", dueAmount!=null && transactionAmount!=null ? dueAmount.subtract(transactionAmount) : (dueAmount==null ? BigDecimal.ZERO : dueAmount));
            transaction.put("amount", transactionAmount==null ? BigDecimal.ZERO : transactionAmount);
            transaction.put("currencyData", currencyData);
            transaction.put("principalPortionDerived", principalPortionDerived==null ? BigDecimal.ZERO : principalPortionDerived);
            transaction.put("interestPortionDerived", interestPortionDerived==null ? BigDecimal.ZERO : interestPortionDerived);

            return transaction;
        }
    }
}