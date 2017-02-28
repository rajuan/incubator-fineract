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
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
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
        // Collection<ClientData> members = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(groupId);

        String sql = "select l.group_id, l.client_id, cl.firstname, cl.lastname, cl.external_id, lt.*, c.* from m_loan l left join m_loan_transaction lt on lt.loan_id = l.id and lt.transaction_type_enum = 2 join m_client cl on cl.id = l.client_id join m_currency c on c.code = l.currency_code where lt.id>0 and l.group_id = ? and lt.transaction_date = ? group by transaction_date order by transaction_date";
        List<Map<String, Object>> transactions = jdbcTemplate.query(sql, new LoanTransactionMapper(), new Object[]{groupId, date});

        sql = "select count(l.id) as num_loans, sum(lt.amount) as transaction_amount, sum(lrs.principal_amount+lrs.interest_amount) as due_amount, lrs.duedate from m_loan l left outer join m_loan_repayment_schedule lrs on lrs.loan_id = l.id left outer join m_loan_transaction lt on lt.loan_id = lrs.loan_id and lt.transaction_type_enum = 2 and lt.is_reversed=0 and lt.transaction_date = lrs.duedate " +
                " where l.group_id = ? and l.loan_type_enum=3" +
                " group by lrs.duedate" +
                " order by lrs.duedate";

        List<Map<String, Object>> schedule = jdbcTemplate.queryForList(sql, new Object[]{groupId});

        Map<String, Object> result = new HashMap<>();
        result.put("group", group);
        result.put("meetings", meetings);
        // result.put("members", members);
        result.put("transactions", transactions);
        result.put("schedule", schedule);
        if(recurringDates!=null && !recurringDates.isEmpty()) {
            result.put("nextMeeting", recurringDates.iterator().next());
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private static final class LoanTransactionMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
            final CurrencyData currencyData = null; // TODO: join with loan table
            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("office_id");
            final Long clientId = rs.getLong("client_id");
            final Long loanId = rs.getLong("loan_id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String externalId = rs.getString("external_id");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final BigDecimal principalPortionDerived = rs.getBigDecimal("principal_portion_derived");
            final BigDecimal interestPortionDerived = rs.getBigDecimal("interest_portion_derived");
            final BigDecimal feeChargesPortionDerived = rs.getBigDecimal("fee_charges_portion_derived");
            final BigDecimal penaltyChargesPortionDerived = rs.getBigDecimal("penalty_charges_portion_derived");
            final BigDecimal overpaymentChargesPortionDerived = rs.getBigDecimal("overpayment_portion_derived");
            final BigDecimal unrecognizedIncomePortion = rs.getBigDecimal("unrecognized_income_portion");
            final BigDecimal outstandingLoanBalanceDerived = rs.getBigDecimal("outstanding_loan_balance_derived");
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transaction_date");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "created_date");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submitted_on_date");
            final Long userId = rs.getLong("appuser_id");
            final Boolean manuallyReversed = rs.getBoolean("manually_adjusted_or_reversed");

            Map<String, Object> transaction = new HashMap<>();

            transaction.put("id", id);
            transaction.put("officeId", officeId);
            transaction.put("loanId", loanId);
            transaction.put("clientId", clientId);
            transaction.put("firstname", firstname);
            transaction.put("lastname", lastname);
            transaction.put("externalId", externalId);
            transaction.put("transactionType", transactionType);
            transaction.put("transactionDate", transactionDate);
            transaction.put("amount", amount);
            transaction.put("currencyData", currencyData);
            transaction.put("principalPortionDerived", principalPortionDerived);
            transaction.put("interestPortionDerived", interestPortionDerived);
            transaction.put("feeChargesPortionDerived", feeChargesPortionDerived);
            transaction.put("penaltyChargesPortionDerived", penaltyChargesPortionDerived);
            transaction.put("overpaymentChargesPortionDerived", overpaymentChargesPortionDerived);
            transaction.put("outstandingLoanBalanceDerived", outstandingLoanBalanceDerived);
            transaction.put("unrecognizedIncomePortion", unrecognizedIncomePortion);
            transaction.put("manuallyReversed", manuallyReversed);
            transaction.put("createdDate", createdDate);
            transaction.put("submittedOnDate", submittedOnDate);
            transaction.put("userId", userId);

            /*
            LoanTransactionData data = new LoanTransactionData(
                    id,
                    officeId,
                    null,
                    transactionType,
                    null,
                    currencyData,
                    transactionDate,
                    amount,
                    principalPortionDerived,
                    interestPortionDerived,
                    feeChargesPortionDerived,
                    penaltyChargesPortionDerived,
                    overpaymentChargesPortionDerived,
                    null,
                    null,
                    null,
                    outstandingLoanBalanceDerived,
                    unrecognizedIncomePortion,
                    manuallyReversed);

            return data;
            */

            return transaction;
        }
    }
}