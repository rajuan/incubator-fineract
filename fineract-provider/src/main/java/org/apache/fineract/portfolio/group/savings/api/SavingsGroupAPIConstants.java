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
package org.apache.fineract.portfolio.group.savings.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface SavingsGroupAPIConstants {

	public static final String CYCLE_RESOURCE_NAME = "sgcycle";
	public static final String FUNDS_RESOURCE_NAME = "sgfund";
	public static final String localeParamName = "locale";
	public static final String dateFormatParamName = "dateFormat";

	// Savings Group Cycle Request/Response common Params
	public static final String expectedNumOfMeetingsParamName = "expectedNumOfMeetings";
	public static final String isShareBasedParamName = "isShareBased";
	public static final String unitPriceOfShareParamName = "unitPriceOfShare";
	public static final String isClientAdditionsAllowedInActiveCycleParamName = "isClientAdditionsAllowedInActiveCycle";
	public static final String isClientExitAllowedInActiveCycleParamName = "isClientExitAllowedInActiveCycle";
	public static final String doesIndividualClientExitForfeitGainsParamName = "doesIndividualClientExitForfeitGains";

	// Savings Group Cycle Request Only Params
	public static final String currencyCodeParamName = "currencyCode";
	public static final String currencyDigitsParamName = "currencyDigits";
	public static final String currencyMultiplesOfParamName = "currencyMultiplesOf";
	public static final String startDateParamName = "startDate";
	public static final String endDateParamName = "endDate";
	public static final String depositsPaymentStrategyIdParamName = "depositsPaymentStrategyId";
	public static final String copyFundsFromPreviousCycle = "copyFundsFromPreviousCycle";

	// Savings Group Cycle Response Only Params
	public static final String cycleNumberParamName = "cycleNumber";
	public static final String statusParamName = "status";
	public static final String currencyParamName = "currency";
	public static final String expectedStartDateParamName = "expectedStartDate";
	public static final String actualStartDateParamName = "actualStartDate";
	public static final String expectedEndDateParamName = "expectedEndDate";
	public static final String actualEndDateParamName = "actualEndDate";
	public static final String numOfMeetingsCompletedParamName = "numOfMeetingsCompleted";
	public static final String numOfMeetingsPendingParamName = "numOfMeetingsPending";
	public static final String depositsPaymentStrategyParamName = "depositsPaymentStrategy";

	public static final String currencyOptionsParamName = "currencyOptions";
	public static final String depositsPaymentStrategyOptionsParamName = "depositsPaymentStrategyOptions";

	// Savings Group Cycle Allowed Params
	public static final Set<String> CYCLE_ALLOWED_REQUEST_PARAMS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName,
					isShareBasedParamName, unitPriceOfShareParamName,
					copyFundsFromPreviousCycle,
					isClientAdditionsAllowedInActiveCycleParamName,
					isClientExitAllowedInActiveCycleParamName,
					doesIndividualClientExitForfeitGainsParamName,
					currencyCodeParamName, currencyDigitsParamName,
					currencyMultiplesOfParamName, startDateParamName,
					endDateParamName, depositsPaymentStrategyIdParamName));

	public static final Set<String> CYCLE_ACTIVATE_ALLOWED_REQUEST_PARAMS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName,
					startDateParamName));

	public static final Set<String> CYCLE_SHAREOUTCLOSE_ALLOWED_REQUEST_PARAMS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName,
					endDateParamName));

	public static final Set<String> CYCLE_ALLOWED_RESPONSE_PARAMS = new HashSet<>(
			Arrays.asList(expectedNumOfMeetingsParamName,
					isShareBasedParamName, unitPriceOfShareParamName,
					unitPriceOfShareParamName, cycleNumberParamName,
					statusParamName, currencyParamName,
					expectedStartDateParamName, actualStartDateParamName,
					expectedEndDateParamName, actualEndDateParamName,
					numOfMeetingsCompletedParamName,
					numOfMeetingsPendingParamName,
					depositsPaymentStrategyParamName, currencyOptionsParamName,
					depositsPaymentStrategyOptionsParamName));

	// Savings Group Funds Request/Response common Params
	public static final String nameParamName = "name";
	public static final String minimumDepositPerMeetingParamName = "minimumDepositPerMeeting";
	public static final String maximumDepositPerMeetingParamName = "maximumDepositPerMeeting";
	public static final String isLoanLimitBasedOnSavingsParamName = "isLoanLimitBasedOnSavings";
	public static final String loanLimitAmountParamName = "loanLimitAmount";
	public static final String loanLimitFactorParamName = "loanLimitFactor";
	public static final String annualNominalInterestRateParamName = "annualNominalInterestRate";
	public static final String repayEveryParamName = "repayEvery";
	public static final String numberOfRepaymentsParamName = "numberOfRepayments";
	public static final String minNumberOfRepaymentsParamName = "minNumberOfRepayments";
	public static final String maxNumberOfRepaymentsParamName = "maxNumberOfRepayments";
	public static final String chargesParamName = "charges";

	// Savings Group Funds Request Params
	public static final String interestMethodIdParamName = "interestMethodId";
	public static final String interestCalculatedInPeriodIdParamName = "interestCalculatedInPeriodId";
	public static final String repaymentPeriodFrequencyIdParamName = "repaymentPeriodFrequencyId";
	public static final String amortizationMethodIdParamName = "amortizationMethodId";
	public static final String transactionProcessingStrategyIdParamName = "transactionProcessingStrategyId";

	// Savings Group Funds Response Params
	// public static final String cycleNumberParamName = "cycleNumber";
	public static final String fundStatusParamName = "fundStatus";
	public static final String totalCashInHandParamName = "totalCashInHand";
	public static final String totalCashInBankParamName = "totalCashInBank";
	public static final String totalDepositsParamName = "totalDeposits";
	public static final String totalLoanPortfolioParamName = "totalLoanPortfolio";
	public static final String totalFeeCollectedParamName = "totalFeeCollected";
	public static final String totalExpensesParamName = "totalExpenses";
	public static final String totalIncomeParamName = "totalIncome";

	public static final String interestMethodParamName = "interestMethod";
	public static final String interestCalculatedInPeriodParamName = "interestCalculatedInPeriod";
	public static final String repaymentPeriodFrequencyParamName = "repaymentPeriodFrequency";
	public static final String amortizationMethodParamName = "amortizationMethod";
	public static final String transactionProcessingStrategyParamName = "transactionProcessingStrategy";

	public static final String interestMethodOptionsParamName = "interestMethodOptions";
	public static final String interestCalculatedInPeriodOptionsParamName = "interestCalculatedInPeriodOptions";
	public static final String repaymentPeriodFrequencyOptionsParamName = "repaymentPeriodFrequencyOptions";
	public static final String amortizationMethodOptionsParamName = "amortizationMethodOptions";
	public static final String transactionProcessingStrategyOptionsParamName = "transactionProcessingStrategyOptions";
	public static final String chargeAppliesToOptionsParamName = "chargeAppliesToOptions";
	public static final String chargeTimeOptionsParamName = "chargeTimeOptions";
	public static final String chargeCalculationOptionsParamName = "chargeCalculationOptions";

	// Savings Group Funds Allowed Params
	public static final Set<String> FUND_ALLOWED_REQUEST_PARAMS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName, nameParamName,
					minimumDepositPerMeetingParamName,
					maximumDepositPerMeetingParamName,
					isLoanLimitBasedOnSavingsParamName,
					loanLimitAmountParamName, loanLimitFactorParamName,
					annualNominalInterestRateParamName, repayEveryParamName,
					numberOfRepaymentsParamName,
					minNumberOfRepaymentsParamName,
					maxNumberOfRepaymentsParamName, chargesParamName,
					interestMethodIdParamName,
					interestCalculatedInPeriodIdParamName,
					repaymentPeriodFrequencyIdParamName,
					amortizationMethodIdParamName,
					transactionProcessingStrategyIdParamName));

	public static final Set<String> FUND_ALLOWED_REQUEST_PARAMS_UPDATE_ACTIVE = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName, nameParamName,
					annualNominalInterestRateParamName, chargesParamName));

	public static final Set<String> FUND_ALLOWED_RESPONSE_PARAMS = new HashSet<>(
			Arrays.asList("id", nameParamName,
					minimumDepositPerMeetingParamName,
					maximumDepositPerMeetingParamName,
					isLoanLimitBasedOnSavingsParamName,
					loanLimitAmountParamName, loanLimitFactorParamName,
					annualNominalInterestRateParamName, repayEveryParamName,
					numberOfRepaymentsParamName,
					minNumberOfRepaymentsParamName,
					maxNumberOfRepaymentsParamName, chargesParamName,
					fundStatusParamName, totalCashInHandParamName,
					totalCashInBankParamName, totalDepositsParamName,
					totalLoanPortfolioParamName, totalFeeCollectedParamName,
					totalExpensesParamName, totalIncomeParamName,
					interestMethodParamName,
					interestCalculatedInPeriodParamName,
					repaymentPeriodFrequencyParamName,
					amortizationMethodParamName,
					transactionProcessingStrategyParamName,
					interestMethodOptionsParamName,
					interestCalculatedInPeriodOptionsParamName,
					repaymentPeriodFrequencyOptionsParamName,
					amortizationMethodOptionsParamName,
					transactionProcessingStrategyOptionsParamName,
					chargeAppliesToOptionsParamName,
					chargeTimeOptionsParamName,
					chargeCalculationOptionsParamName));

	// Savings Group Funds Charge Common Params
	public static final String amountParamName = "amount";
	public static final String activeParamName = "active";
	public static final String penaltyParamName = "penalty";
	// Savings Group Funds Charge Request Params
	public static final String chargeAppliesToIdParamName = "chargeAppliesToId";
	public static final String chargeTimeIdParamName = "chargeTimeId";
	public static final String chargeCalculationIdParamName = "chargeCalculationId";
	// Savings Group Funds Charge Response Params
	public static final String chargeAppliesToEnumParamName = "chargeAppliesToEnum";
	public static final String chargeTimeEnumParamName = "chargeTimeEnum";
	public static final String chargeCalculationEnumParamName = "chargeCalculationEnum";

	// Savings Group Funds Charge Allowed Params
	public static final Set<String> CHARGES_ALLOWED_REQUEST_PARAMS = new HashSet<>(
			Arrays.asList(localeParamName, amountParamName, activeParamName,
					penaltyParamName, chargeAppliesToIdParamName,
					chargeTimeIdParamName, chargeCalculationIdParamName));
	public static final Set<String> CHARGES_ALLOWED_REQUEST_PARAMS_FOR_UPDATE_EXISTING = new HashSet<>(
			Arrays.asList(localeParamName, "id", amountParamName,
					activeParamName));

	// Savings Group Funds Charge Allowed Params
	public static final Set<String> CHARGES_ALLOWED_RESPONSE_PARAMS = new HashSet<>(
			Arrays.asList("id", amountParamName, activeParamName,
					penaltyParamName, chargeAppliesToEnumParamName,
					chargeTimeEnumParamName, chargeCalculationEnumParamName));

}
