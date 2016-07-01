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
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupChargeData;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupFundData;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeAppliesToEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeCalculationEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeTimeEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupFundStatusEnum;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingsGroupFundsReadPlatformServiceImpl implements
		SavingsGroupFundsReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public SavingsGroupFundsReadPlatformServiceImpl(
			final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<SavingsGroupFundData> getLatestCycleFundsData(
			final Long groupId) {
		try {
			SGFundMapper mapper = new SGFundMapper();
			String sql = mapper.baseSql
					+ " where fund.group_id = ? "
					+ " and fund.fund_status = 1 "
					+ " and cycle.cycle_num = (select max(c1.cycle_num) from m_savings_group_cycle as c1 where c1.group_id = ?)";
			return this.jdbcTemplate.query(sql, mapper, new Object[] { groupId,
					groupId });
		} catch (final EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public SavingsGroupFundData getSavingsGroupFundData(final Long fundId, 
			final Long groupId) {
		try {
			SGFundMapper mapper = new SGFundMapper();
			String sql = mapper.baseSql + " where fund.id = ? "
					+ " and grp.id = ? ";
			return this.jdbcTemplate.queryForObject(sql, mapper,
					new Object[] { fundId , groupId});
		} catch (final EmptyResultDataAccessException e) {
			return null;
		}
	}

	private class SGFundMapper implements RowMapper<SavingsGroupFundData> {
		private final String baseSql = new StringBuilder(
				" select fund.id as id, ")
				.append(" fund.name as name, ")
				.append(" fund.minimum_deposit_per_meeting as minimumDepositPerMeeting, ")
				.append(" fund.maximum_deposit_per_meeting as maximumDepositPerMeeting, ")
				.append(" cycle.cycle_num as cycleNumber, ")
				.append(" fund.fund_status as fundStatus, ")
				.append(" fund.total_cash_in_hand as totalCashInHand, ")
				.append(" fund.total_cash_in_bank as totalCashInBank, ")
				.append(" fund.total_deposits as totalDeposits, ")
				.append(" fund.total_loan_portfolio as totalLoanPortfolio, ")
				.append(" fund.total_fee_collected as totalFeeCollected, ")
				.append(" fund.total_expenses as totalExpenses, ")
				.append(" fund.total_income as totalIncome, ")
				.append(" fund.is_loan_limit_based_on_savings as isLoanLimitBasedOnSavings, ")
				.append(" fund.loan_limit_amount as loanLimitAmount, ")
				.append(" fund.loan_limit_factor as loanLimitFactor, ")
				.append(" lndet.annual_nominal_interest_rate as annualNominalInterestRate, ")
				.append(" lndet.interest_method_enum as interestMethod, ")
				.append(" lndet.interest_calculated_in_period_enum as interestCalculatedInPeriod, ")
				.append(" lndet.repay_every as repayEvery, ")
				.append(" lndet.repayment_period_frequency_enum as repaymentPeriodFrequency, ")
				.append(" lndet.number_of_repayments as numberOfRepayments, ")
				.append(" lndet.min_number_of_repayments as minNumberOfRepayments, ")
				.append(" lndet.max_number_of_repayments as maxNumberOfRepayments, ")
				.append(" lndet.amortization_method_enum as amortizationMethod, ")
				.append(" lndet.loan_transaction_strategy_id as transactionProcessingStrategyId, ")
				.append(" ltps.code as transactionProcessingStrategyCode, ")
				.append(" ltps.name as transactionProcessingStrategyName ")
				.append(" from m_savings_group_funds as fund ")
				.append(" left join m_savings_group_cycle as cycle on cycle.id = fund.cycle_id ")
				.append(" left join m_group as grp on (fund.group_id = grp.id and grp.group_type_enum = 2) ")
				.append(" left join m_savings_group_fund_loan_product_details as lndet on fund.id = lndet.fund_id ")
				.append(" left join ref_loan_transaction_processing_strategy ltps on ltps.id = lndet.loan_transaction_strategy_id ")
				.toString();

		@Override
		public SavingsGroupFundData mapRow(ResultSet rs,
				@SuppressWarnings("unused") int rowNum) throws SQLException {
			final Long id = JdbcSupport.getLong(rs, "id");
			final String name = rs.getString("name");
			final BigDecimal minimumDepositPerMeeting = rs
					.getBigDecimal("minimumDepositPerMeeting");
			final BigDecimal maximumDepositPerMeeting = rs
					.getBigDecimal("maximumDepositPerMeeting");
			final Long cycleNumber = JdbcSupport.getLong(rs, "cycleNumber");
			final EnumOptionData fundStatus = SavingsGroupFundStatusEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs, "fundStatus"));
			final BigDecimal totalCashInHand = rs
					.getBigDecimal("totalCashInHand");
			final BigDecimal totalCashInBank = rs
					.getBigDecimal("totalCashInBank");
			final BigDecimal totalDeposits = rs.getBigDecimal("totalDeposits");
			final BigDecimal totalLoanPortfolio = rs
					.getBigDecimal("totalLoanPortfolio");
			final BigDecimal totalFeeCollected = rs
					.getBigDecimal("totalFeeCollected");
			final BigDecimal totalExpenses = rs.getBigDecimal("totalExpenses");
			final BigDecimal totalIncome = rs.getBigDecimal("totalIncome");
			final Boolean isLoanLimitBasedOnSavings = rs
					.getBoolean("isLoanLimitBasedOnSavings");
			final BigDecimal loanLimitAmount = rs
					.getBigDecimal("loanLimitAmount");
			final Integer loanLimitFactor = JdbcSupport.getInteger(rs,
					"loanLimitFactor");
			final BigDecimal annualNominalInterestRate = rs
					.getBigDecimal("annualNominalInterestRate");
			final EnumOptionData interestMethod = LoanEnumerations
					.interestType(JdbcSupport.getInteger(rs, "interestMethod"));
			final EnumOptionData interestCalculatedInPeriod = LoanEnumerations
					.interestCalculationPeriodType(JdbcSupport.getInteger(rs,
							"interestCalculatedInPeriod"));
			final Integer repayEvery = JdbcSupport.getInteger(rs, "repayEvery");
			final EnumOptionData repaymentPeriodFrequency = LoanEnumerations
					.repaymentFrequencyType(JdbcSupport.getInteger(rs,
							"repaymentPeriodFrequency"));
			final Integer numberOfRepayments = JdbcSupport.getInteger(rs,
					"numberOfRepayments");
			final Integer minNumberOfRepayments = JdbcSupport.getInteger(rs,
					"minNumberOfRepayments");
			final Integer maxNumberOfRepayments = JdbcSupport.getInteger(rs,
					"maxNumberOfRepayments");
			final EnumOptionData amortizationMethod = LoanEnumerations
					.amortizationType(JdbcSupport.getInteger(rs,
							"amortizationMethod"));
			final Long transactionProcessingStrategyId = JdbcSupport.getLong(
					rs, "transactionProcessingStrategyId");
			final String transactionProcessingStrategyCode = rs
					.getString("transactionProcessingStrategyCode");
			final String transactionProcessingStrategyName = rs
					.getString("transactionProcessingStrategyName");
			final EnumOptionData transactionProcessingStrategy = new EnumOptionData(
					transactionProcessingStrategyId,
					transactionProcessingStrategyCode,
					transactionProcessingStrategyName);
			Collection<SavingsGroupChargeData> charges = null;
			try {
				SGChargeMapper chargesMapper = new SGChargeMapper();
				final String sql = chargesMapper.baseSql
						+ " where fund.id = ? ";
				charges = jdbcTemplate.query(sql, chargesMapper,
						new Object[] { id });
			} catch (final EmptyResultDataAccessException e) {
				// do nothing
			}

			return SavingsGroupFundData.with(id, name,
					minimumDepositPerMeeting, maximumDepositPerMeeting,
					cycleNumber, fundStatus, totalCashInHand, totalCashInBank,
					totalDeposits, totalLoanPortfolio, totalFeeCollected,
					totalExpenses, totalIncome, isLoanLimitBasedOnSavings,
					loanLimitAmount, loanLimitFactor,
					annualNominalInterestRate, interestMethod,
					interestCalculatedInPeriod, repayEvery,
					repaymentPeriodFrequency, numberOfRepayments,
					minNumberOfRepayments, maxNumberOfRepayments,
					amortizationMethod, transactionProcessingStrategy, charges);
		}

	}

	private class SGChargeMapper implements RowMapper<SavingsGroupChargeData> {
		private final String baseSql = new StringBuilder(
				" select chgs.id as chargeId, ")
				.append(" chgs.charge_applies_to_enum as chargeAppliesToEnum, ")
				.append(" chgs.charge_time_enum as chargeTimeEnum, ")
				.append(" chgs.charge_calculation_enum as chargeCalculationEnum, ")
				.append(" chgs.amount as amount, ")
				.append(" chgs.is_active as active, ")
				.append(" chgs.is_penalty as penalty ")
				.append(" from m_savings_group_funds as fund ")
				.append(" left join m_savings_group_charges as chgs on chgs.fund_id = fund.id ")
				.toString();

		@Override
		public SavingsGroupChargeData mapRow(ResultSet rs,
				@SuppressWarnings("unused") int rowNum) throws SQLException {
			final Long id = JdbcSupport.getLong(rs, "chargeId");
			final EnumOptionData chargeAppliesToEnum = SavingsGroupChargeAppliesToEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs,
							"chargeAppliesToEnum"));
			final EnumOptionData chargeTimeEnum = SavingsGroupChargeTimeEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs,
							"chargeTimeEnum"));
			final EnumOptionData chargeCalculationEnum = SavingsGroupChargeCalculationEnum
					.getEnumOptionData(JdbcSupport.getInteger(rs,
							"chargeCalculationEnum"));
			final BigDecimal amount = rs.getBigDecimal("amount");
			final Boolean active = rs.getBoolean("active");
			final Boolean penalty = rs.getBoolean("penalty");

			return new SavingsGroupChargeData(id, chargeAppliesToEnum,
					chargeTimeEnum, chargeCalculationEnum, amount, active,
					penalty);
		}

	}
}
