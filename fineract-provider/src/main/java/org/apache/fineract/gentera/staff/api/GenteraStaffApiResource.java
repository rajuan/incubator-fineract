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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/gentera/staff/{staffId}/groups")
@Component
@Scope("singleton")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class GenteraStaffApiResource {
    private static final Logger logger = LoggerFactory.getLogger(GenteraStaffApiResource.class);

    private final JdbcTemplate jdbcTemplate;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final StaffReadPlatformService staffReadPlatformService;

    @Autowired
    public GenteraStaffApiResource(final RoutingDataSource dataSource,
                                   final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
                                   final StaffReadPlatformService staffReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.staffReadPlatformService = staffReadPlatformService;
    }

    @GET
    public String staffGroups(@PathParam("staffId") final Long staffId) {
        List<Map<String, Object>> groups = getGroupsNextMeetingDataForStaff(staffId);

        Map<String, Object> staffResult = new HashMap<>();
        staffResult.put("staff", staffReadPlatformService.retrieveStaff(staffId));
        staffResult.put("groups", groups);

        return this.toApiJsonSerializer.serialize(staffResult);
    }

    private List<Map<String,Object>> getGroupsNextMeetingDataForStaff(Long staffId) {
        StringBuilder sql = new StringBuilder("select tmp.group_id, " )
                .append("tmp.duedate, " )
                .append("count(tmp.id) as num_loans, " )
                .append("sum(tmp.orig_due_amount) as orig_due_amount, " )
                .append("sum(paid_amount) as paid_amount, " )
                .append("sum(writtenoff_amount) as writtenoff_amount, " )
                .append("sum(waived_amount) as waived_amount, " )
                .append("(select  " )
                .append("max(lc.loan_cycle) as loan_cycle  " )
                .append("from  " )
                .append("m_loan l1, loan_cycle lc  " )
                .append("where  " )
                .append("l1.client_id = lc.client_id and l1.group_id = tmp.group_id " )
                .append("group by  " )
                .append("l1.group_id) as loan_cycle " )
                .append("from (select l.group_id,  " )
                .append("min(sch.duedate) as duedate,  " )
                .append("l.id as id,  " )
                .append("ifnull(sch.principal_amount,0) + ifnull(sch.interest_amount,0)   " )
                .append(" + ifnull(sch.fee_charges_amount,0) + ifnull(sch.penalty_charges_amount,0) as orig_due_amount,  " )
                .append("ifnull(sch.principal_completed_derived,0) + ifnull(sch.interest_completed_derived,0)   " )
                .append(" + ifnull(sch.fee_charges_completed_derived,0) + ifnull(sch.penalty_charges_completed_derived,0) as paid_amount,  " )
                .append("ifnull(sch.interest_writtenoff_derived,0)   " )
                .append(" + ifnull(sch.fee_charges_writtenoff_derived,0) + ifnull(sch.penalty_charges_writtenoff_derived,0) as writtenoff_amount,  " )
                .append("ifnull(sch.interest_waived_derived,0)   " )
                .append(" + ifnull(sch.fee_charges_waived_derived,0) + ifnull(sch.penalty_charges_waived_derived,0) as waived_amount  " )
                .append("from m_group as g " )
                .append("join m_loan as l on l.group_id = g.id " )
                .append("join m_loan_repayment_schedule as sch on sch.loan_id = l.id  " )
                .append("where l.loan_status_id = 300  " )
                .append("and l.loan_type_enum = 3  " )
                .append("and sch.duedate >= now() " )
                .append("and g.staff_id = ? " )
                .append("group by g.id, l.id) as tmp " )
                .append("group by tmp.group_id " )
                .append("order by tmp.duedate asc ");
        return jdbcTemplate.query(sql.toString(), new LoanScheduleMapper(), new Object[]{staffId});
    }

    private static final class LoanScheduleMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long groupId = rs.getLong("group_id");
            final Long numLoans = rs.getLong("num_loans");
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "duedate");
            final BigDecimal origDueAmount = rs.getBigDecimal("orig_due_amount");
            final BigDecimal paidAmount = rs.getBigDecimal("paid_amount");
            final BigDecimal writtenOffAmount = rs.getBigDecimal("writtenoff_amount");
            final BigDecimal waivedAmount = rs.getBigDecimal("waived_amount");
            final Integer loanCycle = rs.getInt("loan_cycle");

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("groupId", groupId);
            schedule.put("numLoans", numLoans);
            schedule.put("origDueAmount", origDueAmount);
            schedule.put("dueAmount", origDueAmount.subtract(paidAmount).subtract(writtenOffAmount).subtract(waivedAmount));
            schedule.put("transactionAmount", paidAmount);
            schedule.put("writtenOffAmount", writtenOffAmount);
            schedule.put("waivedAmount", waivedAmount);
            schedule.put("duedate", rs.getDate("duedate"));
            schedule.put("duedateAlt", dueDate);
            schedule.put("paymentdate", dueDate);
            schedule.put("loanCycle", loanCycle);

            return schedule;
        }
    }
}