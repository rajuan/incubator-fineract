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
package org.apache.fineract.gentera.group.api;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
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

@Path("/gentera/groups/{groupId}/status")
@Component
@Scope("singleton")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class GenteraGroupApiResource {
    private static final Logger logger = LoggerFactory.getLogger(GenteraGroupApiResource.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GenteraGroupApiResource(final RoutingDataSource dataSource,
                                   final PlatformSecurityContext context,
                                   final ApiRequestParameterHelper apiRequestParameterHelper,
                                   final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
                                   final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    public String groupStatus(@PathParam("groupId") final Long groupId) {
        String sql = "select " +
                "gc.group_id, " +
                "min(IF(l.disbursedon_date is not null, true, false)) as status_disbursed, " +
                "min(IF(l.approvedon_date is not null, true, false)) as status_approved, " +
                "sum(l.total_expected_repayment_derived) as sum_total_expected_repayment_derived, " +
                "sum(l.total_repayment_derived) as sum_total_repayment_derived, " +
                "sum(l.total_expected_costofloan_derived) as sum_total_expected_costofloan_derived, " +
                "sum(l.total_costofloan_derived) as sum_total_costofloan_derived, " +
                "sum(l.total_waived_derived) as sum_total_waived_derived, " +
                "sum(l.total_writtenoff_derived) as sum_total_writtenoff_derived, " +
                "sum(l.total_outstanding_derived) as sum_total_outstanding_derived, " +
                "sum(l.total_overpaid_derived) as sum_total_overpaid_derived " +
                "from m_group g " +
                "left outer join m_group_client gc on g.id = gc.group_id " +
                "left outer join m_client cl on cl.id = gc.client_id " +
                "left outer join m_loan l on cl.id = l.client_id " +
                "where " +
                "gc.group_id = ? " +
                "group by gc.group_id";

        List<Map<String, Object>> statuses = jdbcTemplate.query(sql, new GroupStatusMapper(), new Object[]{groupId});

        return this.toApiJsonSerializer.serialize(statuses);
    }

    private static final class GroupStatusMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            final Boolean statusDisbursed = rs.getBoolean("status_disbursed");
            final Boolean statusApproved = rs.getBoolean("status_approved");
            final BigDecimal sumTotalExpectedRepaymentDerived = rs.getBigDecimal("sum_total_expected_repayment_derived");
            final BigDecimal sumTotalRepaymentDerived = rs.getBigDecimal("sum_total_repayment_derived");
            final BigDecimal sumTotalCostofloanDerived = rs.getBigDecimal("sum_total_costofloan_derived");
            final BigDecimal sumTotalExpectedCostofloanDerived = rs.getBigDecimal("sum_total_expected_costofloan_derived");
            final BigDecimal sumTotalWaivedDerived = rs.getBigDecimal("sum_total_waived_derived");
            final BigDecimal sumTotalWrittenoffDerived = rs.getBigDecimal("sum_total_writtenoff_derived");
            final BigDecimal sumTotalOutstandingDerived = rs.getBigDecimal("sum_total_outstanding_derived");
            final BigDecimal sumTotalOverpaidDerived = rs.getBigDecimal("sum_total_overpaid_derived");

            Map<String, Object> status = new HashMap<>();

            status.put("statusDisbursed", statusDisbursed);
            status.put("statusApproved", statusApproved);
            status.put("sumTotalExpectedRepaymentDerived", sumTotalExpectedRepaymentDerived);
            status.put("sumTotalRepaymentDerived", sumTotalRepaymentDerived);
            status.put("sumTotalCostofloanDerived", sumTotalCostofloanDerived);
            status.put("sumTotalExpectedCostofloanDerived", sumTotalExpectedCostofloanDerived);
            status.put("sumTotalWaivedDerived", sumTotalWaivedDerived);
            status.put("sumTotalWrittenoffDerived", sumTotalWrittenoffDerived);
            status.put("sumTotalOutstandingDerived", sumTotalOutstandingDerived);
            status.put("sumTotalOverpaidDerived", sumTotalOverpaidDerived);

            return status;
        }
    }
}