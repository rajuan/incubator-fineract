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
package org.apache.fineract.portfolio.group.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupCycleData;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupCycleStatusEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupDepositsPaymentStrategyEnum;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingsGroupCycleReadPlatformServiceImpl implements
		SavingsGroupCycleReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public SavingsGroupCycleReadPlatformServiceImpl(
			final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public SavingsGroupCycleData getLatestCycleData(final Long groupId) {
		try {
			SGCycleMapper mapper = new SGCycleMapper();
			String sql = mapper.baseSql
					+ " where c.group_id = ? "
					+ " and c.cycle_num = (select max(c1.cycle_num) from m_savings_group_cycle as c1 where c1.group_id = ?)";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {
					groupId, groupId });
		} catch (final EmptyResultDataAccessException e) {
			return null;
		}
	}

	private class SGCycleMapper implements RowMapper<SavingsGroupCycleData> {
		private final String baseSql = new StringBuilder(
				"select c.id as id, c.cycle_num as cycleNumber, ")
				.append(" c.status_enum as statusEnum, ")
				.append(" c.currency_code as currencyCode, ")
				.append(" c.currency_digits as currencyDigits, ")
				.append(" c.currency_multiplesof as currencyMultiplesOf, ")
				.append(" curr.name as currencyName, ")
				.append(" curr.internationalized_name_code as currencyNameCode, ")
				.append(" curr.display_symbol as currencyDisplaySymbol, ")
				.append(" c.expected_start_date as expectedStartDate, ")
				.append(" c.actual_start_date as actualStartDate, ")
				.append(" c.expected_end_date as expectedEndDate, ")
				.append(" c.actual_end_date as actualEndDate, ")
				.append(" c.expected_num_of_meetings as expectedNumOfMeetings, ")
				.append(" c.num_of_meetings_completed as numOfMeetingsCompleted, ")
				.append(" c.num_of_meetings_pending as numOfMeetingsPending, ")
				.append(" c.is_share_based as isSharesBased, ")
				.append(" c.unit_price_of_share as unitPriceOfShare, ")
				.append(" c.share_product_id as shareProductId, ")
				.append(" c.is_client_additions_allowed_in_active_cycle as clientAddAllowed, ")
				.append(" c.is_client_exit_allowed_in_active_cycle as clientExitAllowed, ")
				.append(" c.does_individual_client_exit_forfeit_gains as exitForfeits, ")
				.append(" c.deposits_payment_strategy as depositStrategy ")
				.append(" from m_savings_group_cycle as c ")
				.append(" left join m_currency as curr on curr.code = c.currency_code ")
				.toString();

		@Override
		public SavingsGroupCycleData mapRow(ResultSet rs,
				@SuppressWarnings("unused") int rowNum) throws SQLException {
			final Long id = JdbcSupport.getLong(rs, "id");
			final Long cycleNumber = JdbcSupport.getLong(rs, "cycleNumber");
			final EnumOptionData status = SavingsGroupCycleStatusEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs, "statusEnum"));
			final String currencyCode = rs.getString("currencyCode");
			final Integer currencyDigits = rs.getInt("currencyDigits");
			final String currencyName = rs.getString("currencyName");
			final String currencyNameCode = rs.getString("currencyNameCode");
			final String currencyDisplaySymbol = rs
					.getString("currencyDisplaySymbol");
			final Integer inMultiplesOf = JdbcSupport.getInteger(rs,
					"currencyMultiplesOf");
			final CurrencyData currency = new CurrencyData(currencyCode,
					currencyName, currencyDigits, inMultiplesOf,
					currencyDisplaySymbol, currencyNameCode);
			final LocalDate expectedStartDate = JdbcSupport.getLocalDate(rs,
					"expectedStartDate");
			final LocalDate actualStartDate = JdbcSupport.getLocalDate(rs,
					"actualStartDate");
			final LocalDate expectedEndDate = JdbcSupport.getLocalDate(rs,
					"expectedEndDate");
			final LocalDate actualEndDate = JdbcSupport.getLocalDate(rs,
					"actualEndDate");
			final Integer expectedNumOfMeetings = JdbcSupport.getInteger(rs,
					"expectedNumOfMeetings");
			final Integer numOfMeetingsCompleted = JdbcSupport.getInteger(rs,
					"numOfMeetingsCompleted");
			final Integer numOfMeetingsPending = JdbcSupport.getInteger(rs,
					"numOfMeetingsPending");
			final Boolean isShareBased = rs.getBoolean("isSharesBased");
			final BigDecimal unitPriceOfShare = rs
					.getBigDecimal("unitPriceOfShare");
			final Long shareProductId = JdbcSupport.getLong(rs,
					"shareProductId");
			final Boolean isClientAdditionsAllowedInActiveCycle = rs
					.getBoolean("clientAddAllowed");
			final Boolean isClientExitAllowedInActiveCycle = rs
					.getBoolean("clientExitAllowed");
			final Boolean doesIndividualClientExitForfeitGains = rs
					.getBoolean("exitForfeits");
			final EnumOptionData depositsPaymentStrategy = SavingsGroupDepositsPaymentStrategyEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs,
							"depositStrategy"));

			return SavingsGroupCycleData.instance(id, cycleNumber, status,
					currency, expectedStartDate, actualStartDate,
					expectedEndDate, actualEndDate, expectedNumOfMeetings,
					numOfMeetingsCompleted, numOfMeetingsPending, isShareBased,
					unitPriceOfShare, shareProductId,
					isClientAdditionsAllowedInActiveCycle,
					isClientExitAllowedInActiveCycle,
					doesIndividualClientExitForfeitGains,
					depositsPaymentStrategy);
		}

	}

}
