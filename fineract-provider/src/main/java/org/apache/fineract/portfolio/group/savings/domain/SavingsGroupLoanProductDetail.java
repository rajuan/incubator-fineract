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
package org.apache.fineract.portfolio.group.savings.domain;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_group_fund_loan_product_details")
public class SavingsGroupLoanProductDetail extends AbstractPersistable<Long> {

	@OneToOne
	@JoinColumn(name = "fund_id", nullable = false)
	private SavingsGroupFund fund;

	@Column(name = "annual_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal annualNominalInterestRate;

	@Column(name = "interest_method_enum", nullable = false)
	private Integer interestMethodEnum;

	@Column(name = "interest_calculated_in_period_enum", nullable = false)
	private Integer interestCalculatedInPeriodEnum;

	@Column(name = "repay_every", nullable = false)
	private Integer repayEvery;

	@Column(name = "repayment_period_frequency_enum", nullable = false)
	private Integer repaymentPeriodFrequencyEnum;

	@Column(name = "number_of_repayments", nullable = false)
	private Integer numberOfRepayments;

	@Column(name = "min_number_of_repayments", nullable = true)
	private Integer minNumberOfRepayments;

	@Column(name = "max_number_of_repayments", nullable = true)
	private Integer maxNumberOfRepayments;

	@Column(name = "amortization_method_enum", nullable = false)
	private Integer amortizationMethodEnum;

	@ManyToOne
	@JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
	private LoanTransactionProcessingStrategy transactionProcessingStrategy;

	protected SavingsGroupLoanProductDetail() {

	}

	public static SavingsGroupLoanProductDetail createWith(final JsonCommand command,
			final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy, 
			final SavingsGroupFund fund) {
		SavingsGroupLoanProductDetail newpd = new SavingsGroupLoanProductDetail();
		newpd.fund = fund;
		newpd.annualNominalInterestRate = command
				.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.annualNominalInterestRateParamName);
		newpd.interestMethodEnum = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.interestMethodIdParamName);
		newpd.interestCalculatedInPeriodEnum = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName);
		newpd.repayEvery = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.repayEveryParamName);
		newpd.repaymentPeriodFrequencyEnum = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName);
		newpd.numberOfRepayments = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.numberOfRepaymentsParamName);
		newpd.minNumberOfRepayments = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.minNumberOfRepaymentsParamName);
		newpd.maxNumberOfRepayments = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.maxNumberOfRepaymentsParamName);
		newpd.amortizationMethodEnum = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.amortizationMethodIdParamName);
		newpd.transactionProcessingStrategy = loanTransactionProcessingStrategy;
		return newpd;
	}

	public LoanTransactionProcessingStrategy getLoanTransactionProcessingStrategy() {
		return this.transactionProcessingStrategy;
	}

	public void setLoanTransactionProcessingStrategy(
			final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy) {

		this.transactionProcessingStrategy = loanTransactionProcessingStrategy;
	}

	public void update(final Map<String, Object> changes,
			final JsonCommand command) {
		if (command.isChangeInBigDecimalParameterNamed(
				SavingsGroupAPIConstants.annualNominalInterestRateParamName,
				this.annualNominalInterestRate)) {
			this.annualNominalInterestRate = command
					.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.annualNominalInterestRateParamName);
			changes.put(
					SavingsGroupAPIConstants.annualNominalInterestRateParamName,
					this.annualNominalInterestRate);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.interestMethodIdParamName,
				this.interestMethodEnum)) {
			this.interestMethodEnum = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.interestMethodIdParamName);
			changes.put(SavingsGroupAPIConstants.interestMethodIdParamName,
					this.interestMethodEnum);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName,
				this.interestCalculatedInPeriodEnum)) {
			this.interestCalculatedInPeriodEnum = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName);
			changes.put(
					SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName,
					this.interestCalculatedInPeriodEnum);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.repayEveryParamName, this.repayEvery)) {
			this.repayEvery = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.repayEveryParamName);
			changes.put(SavingsGroupAPIConstants.repayEveryParamName,
					this.repayEvery);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName,
				this.repaymentPeriodFrequencyEnum)) {
			this.repaymentPeriodFrequencyEnum = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName);
			changes.put(
					SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName,
					this.repaymentPeriodFrequencyEnum);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.numberOfRepaymentsParamName,
				this.numberOfRepayments)) {
			this.numberOfRepayments = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.numberOfRepaymentsParamName);
			changes.put(SavingsGroupAPIConstants.numberOfRepaymentsParamName,
					this.numberOfRepayments);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.minNumberOfRepaymentsParamName,
				this.minNumberOfRepayments)) {
			this.minNumberOfRepayments = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.minNumberOfRepaymentsParamName);
			changes.put(
					SavingsGroupAPIConstants.minNumberOfRepaymentsParamName,
					this.minNumberOfRepayments);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.maxNumberOfRepaymentsParamName,
				this.maxNumberOfRepayments)) {
			this.maxNumberOfRepayments = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.maxNumberOfRepaymentsParamName);
			changes.put(
					SavingsGroupAPIConstants.maxNumberOfRepaymentsParamName,
					this.maxNumberOfRepayments);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.amortizationMethodIdParamName,
				this.amortizationMethodEnum)) {
			this.amortizationMethodEnum = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.amortizationMethodIdParamName);
			changes.put(SavingsGroupAPIConstants.amortizationMethodIdParamName,
					this.amortizationMethodEnum);
		}
	}

	public SavingsGroupLoanProductDetail copy(final SavingsGroupFund fund) {
		SavingsGroupLoanProductDetail newpd = new SavingsGroupLoanProductDetail();
		newpd.fund = fund;
		newpd.annualNominalInterestRate = this.annualNominalInterestRate;
		newpd.interestMethodEnum = this.interestMethodEnum;
		newpd.interestCalculatedInPeriodEnum = this.interestCalculatedInPeriodEnum;
		newpd.repayEvery = this.repayEvery;
		newpd.repaymentPeriodFrequencyEnum = this.repaymentPeriodFrequencyEnum;
		newpd.numberOfRepayments = this.numberOfRepayments;
		newpd.minNumberOfRepayments = this.minNumberOfRepayments;
		newpd.maxNumberOfRepayments = this.maxNumberOfRepayments;
		newpd.amortizationMethodEnum = this.amortizationMethodEnum;
		newpd.transactionProcessingStrategy = this.transactionProcessingStrategy;
		return newpd;
	}
}
