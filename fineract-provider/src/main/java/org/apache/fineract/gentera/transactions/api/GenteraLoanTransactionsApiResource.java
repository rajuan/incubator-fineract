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

import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
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
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final GroupReadPlatformService groupReadPlatformService;

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
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupReadPlatformService = groupReadPlatformService;
    }

    @GET
    public String groupMeetingTransactions(@PathParam("groupId") final Long groupId, @QueryParam("date") @DefaultValue("") String date) {
        Map<String, Object> result = new HashMap<>();

        GroupGeneralData group = groupReadPlatformService.retrieveOne(groupId);
        result.put("group", group);

        List<Map<String, Object>> schedule = getSchedule(groupId);
        result.put("schedule", schedule);

        LocalDate now = LocalDate.now();
        LocalDate nextMeeting = null;
        LocalDate paymentDate = null;

        for(Map<String, Object> s : schedule) {
            LocalDate duedate = (LocalDate)s.get("duedate_alt");
            LocalDate pd = (LocalDate)s.get("paymentdate");
            BigDecimal dueAmount = (BigDecimal)s.get("due_amount");

            if(!duedate.isBefore(now)) {
                logger.warn(">>> WAS SAME DATE: {} {}", dueAmount, duedate);
                nextMeeting = duedate;
                paymentDate = pd;
                break;
            }
        }

        result.put("nextMeeting", nextMeeting);

        if(StringUtils.isEmpty(date)) {
            result.put("transactions", getTransactions(groupId, paymentDate, nextMeeting));
        } else {
            LocalDate d = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(date);
            // TODO: we should check for holidays and move to next
            result.put("transactions", getTransactions(groupId, d, d));
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private List<Map<String, Object>> getTransactions(Long groupId, LocalDate paymentdate, LocalDate date) {
        logger.warn(">> Retrieve group {} at {}", groupId, date);
        StringBuilder sql = new StringBuilder("select l.group_id, ")
                .append("l.client_id, ")
                .append("cl.firstname, ")
                .append("cl.lastname, ")
                .append("cl.external_id, ")
                .append("cv.code_value as role, ")
                .append("sch.duedate as duedate, " )
                .append("ai.additional_family_name, " )
                .append("ai.account_number, " )
                .append("l.id as orig_loan_id,  " )
                .append("l.currency_code,  " )
                .append("count(trmap.id) as num_transactions, " )
                .append("ifnull(sch.principal_amount,0) + ifnull(sch.interest_amount,0)  ")
                .append(" + ifnull(sch.fee_charges_amount,0) + ifnull(sch.penalty_charges_amount,0) as due_amount, ")
                .append("ifnull(sch.principal_completed_derived,0) + ifnull(sch.interest_completed_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_completed_derived,0) + ifnull(sch.penalty_charges_completed_derived,0) as transaction_amount, ")
                .append("ifnull(sch.interest_writtenoff_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_writtenoff_derived,0) + ifnull(sch.penalty_charges_writtenoff_derived,0) as writtenoff_amount, ")
                .append("ifnull(sch.interest_waived_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_waived_derived,0) + ifnull(sch.penalty_charges_waived_derived,0) as waived_amount, ")
                .append("ifnull(sch.principal_completed_derived,0) as principal_portion_derived, ")
                .append("ifnull(sch.interest_completed_derived,0) as interest_portion_derived ")
                .append("from m_loan as l " )
                .append("left join m_loan_repayment_schedule as sch on sch.loan_id = l.id " )
                .append("left join m_client as cl on l.client_id = cl.id " )
                .append("left join addition_information as ai on ai.client_id = cl.id " )
                .append("left join m_loan_transaction_repayment_schedule_mapping as trmap on trmap.loan_repayment_schedule_id = sch.id " )
                .append("left join m_group_roles gr on cl.id = gr.client_id and gr.group_id = l.group_id " )
                .append("left join m_code_value cv on cv.code_id = gr.role_cv_id and cv.is_active = true and cv.code_value = 'Leader'  " )
                .append("where l.loan_status_id = 300 " )
                .append("and l.loan_type_enum = 3 " )
                .append("and l.group_id = ? " )
                .append("and sch.duedate = ? " )
                .append("group by l.id " )
                .append("order by l.id asc ");

        return jdbcTemplate.query(sql.toString(), new LoanTransactionMapper(), new Object[]{groupId, date.toString("yyyy-MM-dd")});
    }

    private List<Map<String, Object>> getSchedule(Long groupId) {
        StringBuilder sql = new StringBuilder("select l.group_id, ")
                .append("count(l.id) as num_loans, ")
                .append("sch.duedate as duedate, ")
                .append("sum(ifnull(sch.principal_amount,0) + ifnull(sch.interest_amount,0)  ")
                .append(" + ifnull(sch.fee_charges_amount,0) + ifnull(sch.penalty_charges_amount,0)) as orig_due_amount, ")
                .append("sum(ifnull(sch.principal_completed_derived,0) + ifnull(sch.interest_completed_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_completed_derived,0) + ifnull(sch.penalty_charges_completed_derived,0)) as paid_amount, ")
                .append("sum(ifnull(sch.interest_writtenoff_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_writtenoff_derived,0) + ifnull(sch.penalty_charges_writtenoff_derived,0)) as writtenoff_amount, ")
                .append("sum(ifnull(sch.interest_waived_derived,0)  ")
                .append(" + ifnull(sch.fee_charges_waived_derived,0) + ifnull(sch.penalty_charges_waived_derived,0)) as waived_amount ")
                .append("from m_loan as l ")
                .append("left join m_loan_repayment_schedule as sch on sch.loan_id = l.id ")
                .append("where l.loan_status_id = 300 ")
                .append("and l.loan_type_enum = 3 ")
                .append("and l.group_id = ? ")
                .append("group by sch.duedate ")
                .append("order by sch.duedate asc");

        List<Map<String, Object>> schedule = jdbcTemplate.query(sql.toString(), new LoanScheduleMapper(), new Object[]{groupId});
        return schedule;
    }

    private static final class LoanScheduleMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            final Long numLoans = rs.getLong("num_loans");
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "duedate");
            final BigDecimal origDueAmount = rs.getBigDecimal("orig_due_amount");
            final BigDecimal paidAmount = rs.getBigDecimal("paid_amount");
            final BigDecimal writtenOffAmount = rs.getBigDecimal("writtenoff_amount");
            final BigDecimal waivedAmount = rs.getBigDecimal("waived_amount");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("num_loans", numLoans);
            schedule.put("due_amount", origDueAmount.subtract(paidAmount).subtract(writtenOffAmount).subtract(waivedAmount));
            schedule.put("transaction_amount", paidAmount);
            schedule.put("writtenOffAmount", writtenOffAmount);
            schedule.put("waived_amount", waivedAmount);
            schedule.put("duedate", dueDate);
            schedule.put("duedate_alt", dueDate);
            schedule.put("paymentdate", dueDate);

            return schedule;
        }
    }

    private static final class LoanTransactionMapper implements RowMapper<Map<String, Object>> {

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
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
            final String role = rs.getString("role");

            Map<String, Object> transaction = new HashMap<>();

            transaction.put("loanId", loanId);
            transaction.put("clientId", clientId);
            transaction.put("firstname", firstname);
            transaction.put("lastname", lastname);
            transaction.put("additionalFamilyName", additionalFamilyName);
            transaction.put("accountNumber", accountNumber);
            transaction.put("externalId", externalId);
            transaction.put("role", role);
            transaction.put("numTransactions", numTransactions);
            transaction.put("currencyCode", currencyCode);
            transaction.put("origDueAmount", dueAmount==null ? BigDecimal.ZERO : dueAmount);
            transaction.put("dueAmount", dueAmount!=null && transactionAmount!=null ? dueAmount.subtract(transactionAmount) : (dueAmount==null ? BigDecimal.ZERO : dueAmount));
            transaction.put("amount", transactionAmount==null ? BigDecimal.ZERO : transactionAmount);
            transaction.put("currencyData", null);
            transaction.put("principalPortionDerived", principalPortionDerived==null ? BigDecimal.ZERO : principalPortionDerived);
            transaction.put("interestPortionDerived", interestPortionDerived==null ? BigDecimal.ZERO : interestPortionDerived);

            return transaction;
        }
    }
}