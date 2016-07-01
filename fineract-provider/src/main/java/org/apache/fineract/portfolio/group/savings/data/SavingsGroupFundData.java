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
package org.apache.fineract.portfolio.group.savings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;

public class SavingsGroupFundData {
	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String name;
	@SuppressWarnings("unused")
	private final BigDecimal minimumDepositPerMeeting;
	@SuppressWarnings("unused")
	private final BigDecimal maximumDepositPerMeeting;
	@SuppressWarnings("unused")
	private final Long cycleNumber;
	@SuppressWarnings("unused")
	private final EnumOptionData fundStatus;
	@SuppressWarnings("unused")
	private final BigDecimal totalCashInHand;
	@SuppressWarnings("unused")
	private final BigDecimal totalCashInBank;
	@SuppressWarnings("unused")
	private final BigDecimal totalDeposits;
	@SuppressWarnings("unused")
	private final BigDecimal totalLoanPortfolio;
	@SuppressWarnings("unused")
	private final BigDecimal totalFeeCollected;
	@SuppressWarnings("unused")
	private final BigDecimal totalExpenses;
	@SuppressWarnings("unused")
	private final BigDecimal totalIncome;
	@SuppressWarnings("unused")
	private final Boolean isLoanLimitBasedOnSavings;
	@SuppressWarnings("unused")
	private final BigDecimal loanLimitAmount;
	@SuppressWarnings("unused")
	private final Integer loanLimitFactor;
	@SuppressWarnings("unused")
	private final BigDecimal annualNominalInterestRate;
	@SuppressWarnings("unused")
	private final EnumOptionData interestMethod;
	@SuppressWarnings("unused")
	private final EnumOptionData interestCalculatedInPeriod;
	@SuppressWarnings("unused")
	private final Integer repayEvery;
	@SuppressWarnings("unused")
	private final EnumOptionData repaymentPeriodFrequency;
	@SuppressWarnings("unused")
	private final Integer numberOfRepayments;
	@SuppressWarnings("unused")
	private final Integer minNumberOfRepayments;
	@SuppressWarnings("unused")
	private final Integer maxNumberOfRepayments;
	@SuppressWarnings("unused")
	private final EnumOptionData amortizationMethod;
	@SuppressWarnings("unused")
	private final EnumOptionData transactionProcessingStrategy;
	@SuppressWarnings("unused")
	private final Collection<SavingsGroupChargeData> charges;

	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> interestMethodOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> interestCalculatedInPeriodOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> repaymentPeriodFrequencyOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> amortizationMethodOptions;
	@SuppressWarnings("unused")
	private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> chargeAppliesToOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> loanChargeTimeOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> loanChargeCalculationOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> groupChargeTimeOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> groupChargeCalculationOptions;

	private SavingsGroupFundData(final Long id, final String name,
			final BigDecimal minimumDepositPerMeeting,
			final BigDecimal maximumDepositPerMeeting, final Long cycleNumber,
			final EnumOptionData fundStatus, final BigDecimal totalCashInHand,
			final BigDecimal totalCashInBank, final BigDecimal totalDeposits,
			final BigDecimal totalLoanPortfolio,
			final BigDecimal totalFeeCollected, final BigDecimal totalExpenses,
			final BigDecimal totalIncome,
			final Boolean isLoanLimitBasedOnSavings,
			final BigDecimal loanLimitAmount, final Integer loanLimitFactor,
			final BigDecimal annualNominalInterestRate,
			final EnumOptionData interestMethod,
			final EnumOptionData interestCalculatedInPeriod,
			final Integer repayEvery,
			final EnumOptionData repaymentPeriodFrequency,
			final Integer numberOfRepayments,
			final Integer minNumberOfRepayments,
			final Integer maxNumberOfRepayments,
			final EnumOptionData amortizationMethod,
			final EnumOptionData transactionProcessingStrategy,
			final Collection<SavingsGroupChargeData> charges) {

		this.id = id;
		this.name = name;
		this.minimumDepositPerMeeting = minimumDepositPerMeeting;
		this.maximumDepositPerMeeting = maximumDepositPerMeeting;
		this.cycleNumber = cycleNumber;
		this.fundStatus = fundStatus;
		this.totalCashInHand = totalCashInHand;
		this.totalCashInBank = totalCashInBank;
		this.totalDeposits = totalDeposits;
		this.totalLoanPortfolio = totalLoanPortfolio;
		this.totalFeeCollected = totalFeeCollected;
		this.totalExpenses = totalExpenses;
		this.totalIncome = totalIncome;
		this.isLoanLimitBasedOnSavings = isLoanLimitBasedOnSavings;
		this.loanLimitAmount = loanLimitAmount;
		this.loanLimitFactor = loanLimitFactor;
		this.annualNominalInterestRate = annualNominalInterestRate;
		this.interestMethod = interestMethod;
		this.interestCalculatedInPeriod = interestCalculatedInPeriod;
		this.repayEvery = repayEvery;
		this.repaymentPeriodFrequency = repaymentPeriodFrequency;
		this.numberOfRepayments = numberOfRepayments;
		this.minNumberOfRepayments = minNumberOfRepayments;
		this.maxNumberOfRepayments = maxNumberOfRepayments;
		this.amortizationMethod = amortizationMethod;
		this.transactionProcessingStrategy = transactionProcessingStrategy;
		this.charges = charges;

		this.interestMethodOptions = null;
		this.interestCalculatedInPeriodOptions = null;
		this.repaymentPeriodFrequencyOptions = null;
		this.amortizationMethodOptions = null;
		this.transactionProcessingStrategyOptions = null;
		this.chargeAppliesToOptions = null;
		this.loanChargeTimeOptions = null;
		this.loanChargeCalculationOptions = null;
		this.groupChargeTimeOptions = null;
		this.groupChargeCalculationOptions = null;
	}

	private SavingsGroupFundData(
			List<EnumOptionData> interestMethodOptions,
			List<EnumOptionData> interestCalculatedInPeriodOptions,
			List<EnumOptionData> repaymentPeriodFrequencyOptions,
			List<EnumOptionData> amortizationMethodOptions,
			Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions,
			List<EnumOptionData> chargeAppliesToOptions,
			List<EnumOptionData> loanChargeTimeOptions,
			List<EnumOptionData> loanChargeCalculationOptions,
			List<EnumOptionData> groupChargeTimeOptions,
			List<EnumOptionData> groupChargeCalculationOptions) {
		this.id = null;
		this.name = null;
		this.minimumDepositPerMeeting = null;
		this.maximumDepositPerMeeting = null;
		this.cycleNumber = null;
		this.fundStatus = null;
		this.totalCashInHand = null;
		this.totalCashInBank = null;
		this.totalDeposits = null;
		this.totalLoanPortfolio = null;
		this.totalFeeCollected = null;
		this.totalExpenses = null;
		this.totalIncome = null;
		this.isLoanLimitBasedOnSavings = null;
		this.loanLimitAmount = null;
		this.loanLimitFactor = null;
		this.annualNominalInterestRate = null;
		this.interestMethod = null;
		this.interestCalculatedInPeriod = null;
		this.repayEvery = null;
		this.repaymentPeriodFrequency = null;
		this.numberOfRepayments = null;
		this.minNumberOfRepayments = null;
		this.maxNumberOfRepayments = null;
		this.amortizationMethod = null;
		this.transactionProcessingStrategy = null;
		this.charges = null;

		this.interestMethodOptions = interestMethodOptions;
		this.interestCalculatedInPeriodOptions = interestCalculatedInPeriodOptions;
		this.repaymentPeriodFrequencyOptions = repaymentPeriodFrequencyOptions;
		this.amortizationMethodOptions = amortizationMethodOptions;
		this.transactionProcessingStrategyOptions = transactionProcessingStrategyOptions;
		this.chargeAppliesToOptions = chargeAppliesToOptions;
		this.loanChargeTimeOptions = loanChargeTimeOptions;
		this.loanChargeCalculationOptions = loanChargeCalculationOptions;
		this.groupChargeTimeOptions = groupChargeTimeOptions;
		this.groupChargeCalculationOptions = groupChargeCalculationOptions;
	}

	public static SavingsGroupFundData template(
			final List<EnumOptionData> interestMethodOptions,
			final List<EnumOptionData> interestCalculatedInPeriodOptions,
			final List<EnumOptionData> repaymentPeriodFrequencyOptions,
			final List<EnumOptionData> amortizationMethodOptions,
			final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions,
			final List<EnumOptionData> chargeAppliesToOptions,
			final List<EnumOptionData> loanChargeTimeOptions,
			final List<EnumOptionData> loanChargeCalculationOptions,
			final List<EnumOptionData> groupChargeTimeOptions,
			final List<EnumOptionData> groupChargeCalculationOptions) {
		return new SavingsGroupFundData(interestMethodOptions,
				interestCalculatedInPeriodOptions,
				repaymentPeriodFrequencyOptions, amortizationMethodOptions,
				transactionProcessingStrategyOptions, chargeAppliesToOptions,
				loanChargeTimeOptions, loanChargeCalculationOptions,
				groupChargeTimeOptions, groupChargeCalculationOptions);
	}

	public static SavingsGroupFundData with(final Long id, final String name,
			final BigDecimal minimumDepositPerMeeting,
			final BigDecimal maximumDepositPerMeeting, final Long cycleNumber,
			final EnumOptionData fundStatus, final BigDecimal totalCashInHand,
			final BigDecimal totalCashInBank, final BigDecimal totalDeposits,
			final BigDecimal totalLoanPortfolio,
			final BigDecimal totalFeeCollected, final BigDecimal totalExpenses,
			final BigDecimal totalIncome,
			final Boolean isLoanLimitBasedOnSavings,
			final BigDecimal loanLimitAmount, final Integer loanLimitFactor,
			final BigDecimal annualNominalInterestRate,
			final EnumOptionData interestMethod,
			final EnumOptionData interestCalculatedInPeriod,
			final Integer repayEvery,
			final EnumOptionData repaymentPeriodFrequency,
			final Integer numberOfRepayments,
			final Integer minNumberOfRepayments,
			final Integer maxNumberOfRepayments,
			final EnumOptionData amortizationMethod,
			final EnumOptionData transactionProcessingStrategy,
			final Collection<SavingsGroupChargeData> charges) {
		return new SavingsGroupFundData(id, name, minimumDepositPerMeeting,
				maximumDepositPerMeeting, cycleNumber, fundStatus,
				totalCashInHand, totalCashInBank, totalDeposits,
				totalLoanPortfolio, totalFeeCollected, totalExpenses,
				totalIncome, isLoanLimitBasedOnSavings, loanLimitAmount,
				loanLimitFactor, annualNominalInterestRate, interestMethod,
				interestCalculatedInPeriod, repayEvery,
				repaymentPeriodFrequency, numberOfRepayments,
				minNumberOfRepayments, maxNumberOfRepayments,
				amortizationMethod, transactionProcessingStrategy, charges);
	}

}
