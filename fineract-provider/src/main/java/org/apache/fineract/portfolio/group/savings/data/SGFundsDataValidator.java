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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SGFundsDataValidator {

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public SGFundsDataValidator(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreateFund(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.FUND_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		final String name = this.fromApiJsonHelper.extractStringNamed(
				SavingsGroupAPIConstants.nameParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.nameParamName).value(name)
				.notBlank().notExceedingLengthOf(50);

		final BigDecimal minimumDepositPerMeeting = this.fromApiJsonHelper
				.extractBigDecimalWithLocaleNamed(
						SavingsGroupAPIConstants.minimumDepositPerMeetingParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.minimumDepositPerMeetingParamName)
				.value(minimumDepositPerMeeting).notNull().positiveAmount();

		final BigDecimal maximumDepositPerMeeting = this.fromApiJsonHelper
				.extractBigDecimalWithLocaleNamed(
						SavingsGroupAPIConstants.maximumDepositPerMeetingParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.maximumDepositPerMeetingParamName)
				.value(maximumDepositPerMeeting).notNull().positiveAmount()
				.notLessThanMin(minimumDepositPerMeeting);

		final Boolean isLoanLimitBasedOnSavings = this.fromApiJsonHelper
				.extractBooleanNamed(
						SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName,
						element);
		if (isLoanLimitBasedOnSavings == null) {
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName)
					.value(isLoanLimitBasedOnSavings)
					.trueOrFalseRequired(false);
		} else {
			validateLoanLimits(baseDataValidator, element,
					isLoanLimitBasedOnSavings);
		}
		final BigDecimal annualNominalInterestRate = this.fromApiJsonHelper
				.extractBigDecimalWithLocaleNamed(
						SavingsGroupAPIConstants.annualNominalInterestRateParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.annualNominalInterestRateParamName)
				.value(annualNominalInterestRate).notNull().positiveAmount();

		final Integer interestMethodEnum = this.fromApiJsonHelper
				.extractIntegerNamed(
						SavingsGroupAPIConstants.interestMethodIdParamName,
						element, Locale.getDefault());
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.interestMethodIdParamName)
				.value(interestMethodEnum).notNull().inMinMaxRange(0, 1);

		final Integer interestCalculatedInPeriodEnum = this.fromApiJsonHelper
				.extractIntegerNamed(
						SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName,
						element, Locale.getDefault());
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName)
				.value(interestCalculatedInPeriodEnum).notNull()
				.inMinMaxRange(0, 1);

		final Integer repayEvery = this.fromApiJsonHelper
				.extractIntegerWithLocaleNamed(
						SavingsGroupAPIConstants.repayEveryParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.repayEveryParamName)
				.value(repayEvery).notNull().integerGreaterThanZero();

		final Integer repaymentPeriodFrequencyEnum = this.fromApiJsonHelper
				.extractIntegerNamed(
						SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName,
						element, Locale.getDefault());
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName)
				.value(repaymentPeriodFrequencyEnum).notNull()
				.inMinMaxRange(0, 3);

		validateMinMaxNumberOfRepayments(baseDataValidator, element);

		final Integer amortizationMethodEnum = this.fromApiJsonHelper
				.extractIntegerNamed(
						SavingsGroupAPIConstants.amortizationMethodIdParamName,
						element, Locale.getDefault());
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.amortizationMethodIdParamName)
				.value(amortizationMethodEnum).notNull().inMinMaxRange(0, 1);

		final Long transactionProcessingStrategyId = this.fromApiJsonHelper
				.extractLongNamed(
						SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName)
				.value(transactionProcessingStrategyId).notNull()
				.integerGreaterThanZero();

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.chargesParamName, element)) {
			JsonArray charges = null;
			try {
				charges = this.fromApiJsonHelper
						.extractJsonArrayNamed(
								SavingsGroupAPIConstants.chargesParamName, element);
				baseDataValidator.reset()
						.parameter(SavingsGroupAPIConstants.chargesParamName)
						.value(charges).notBlank().jsonArrayNotEmpty();
			} catch (Exception e) {
				baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.chargesParamName)
				.value(charges).failWithCode("not.array", "Expected Array");;
			}
			if (charges != null) {
				for (int i = 0; i < charges.size(); i++) {
					final JsonElement charge = charges.get(i);
					validateNewChargeDef(baseDataValidator, i, charge);
				}
			}
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForUpdateFundInitiateStatus(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.FUND_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.nameParamName, element)) {
			final String name = this.fromApiJsonHelper.extractStringNamed(
					SavingsGroupAPIConstants.nameParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.nameParamName)
					.value(name).notBlank().notExceedingLengthOf(50);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.minimumDepositPerMeetingParamName,
				element)) {
			final BigDecimal minimumDepositPerMeeting = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.minimumDepositPerMeetingParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.minimumDepositPerMeetingParamName)
					.value(minimumDepositPerMeeting).notNull().positiveAmount();

			final BigDecimal maximumDepositPerMeeting = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.maximumDepositPerMeetingParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.maximumDepositPerMeetingParamName)
					.value(maximumDepositPerMeeting).notNull().positiveAmount()
					.notLessThanMin(minimumDepositPerMeeting);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName,
				element)) {
			final Boolean isLoanLimitBasedOnSavings = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName,
							element);
			if (isLoanLimitBasedOnSavings == null) {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName)
						.value(isLoanLimitBasedOnSavings)
						.trueOrFalseRequired(false);
			} else
				validateLoanLimits(baseDataValidator, element,
						isLoanLimitBasedOnSavings);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.annualNominalInterestRateParamName,
				element)) {
			final BigDecimal annualNominalInterestRate = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.annualNominalInterestRateParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.annualNominalInterestRateParamName)
					.value(annualNominalInterestRate).notNull()
					.positiveAmount();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.interestMethodIdParamName, element)) {
			final Integer interestMethodEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.interestMethodIdParamName,
							element, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.interestMethodIdParamName)
					.value(interestMethodEnum).notNull().inMinMaxRange(0, 1);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName,
				element)) {
			final Integer interestCalculatedInPeriodEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName,
							element, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.interestCalculatedInPeriodIdParamName)
					.value(interestCalculatedInPeriodEnum).notNull()
					.inMinMaxRange(0, 1);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.repayEveryParamName, element)) {
			final Integer repayEvery = this.fromApiJsonHelper
					.extractIntegerWithLocaleNamed(
							SavingsGroupAPIConstants.repayEveryParamName,
							element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.repayEveryParamName)
					.value(repayEvery).notNull().integerGreaterThanZero();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName,
				element)) {
			final Integer repaymentPeriodFrequencyEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName,
							element, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.repaymentPeriodFrequencyIdParamName)
					.value(repaymentPeriodFrequencyEnum).notNull()
					.inMinMaxRange(0, 3);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.numberOfRepaymentsParamName, element)) {
			validateMinMaxNumberOfRepayments(baseDataValidator, element);
		}

		if (this.fromApiJsonHelper
				.parameterExists(
						SavingsGroupAPIConstants.amortizationMethodIdParamName,
						element)) {
			final Integer amortizationMethodEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.amortizationMethodIdParamName,
							element, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.amortizationMethodIdParamName)
					.value(amortizationMethodEnum).notNull()
					.inMinMaxRange(0, 1);
		}

		if (this.fromApiJsonHelper
				.parameterExists(
						SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName,
						element)) {
			final Long transactionProcessingStrategyId = this.fromApiJsonHelper
					.extractLongNamed(
							SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName)
					.value(transactionProcessingStrategyId).notNull()
					.integerGreaterThanZero();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.chargesParamName, element)) {
			final JsonArray charges = this.fromApiJsonHelper
					.extractJsonArrayNamed(
							SavingsGroupAPIConstants.chargesParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.chargesParamName)
					.value(charges).notBlank().jsonArrayNotEmpty();
			if (charges != null) {
				for (int i = 0; i < charges.size(); i++) {
					final JsonElement charge = charges.get(i);

					if (this.fromApiJsonHelper.parameterExists("id", charge)) {
						validateChargeExisting(baseDataValidator, element, i,
								charge);
					} else {
						validateNewChargeDef(baseDataValidator, i, charge);
					}
				}
			}
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForUpdateFundActiveStatus(final JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper
				.checkForUnsupportedParameters(
						typeOfMap,
						json,
						SavingsGroupAPIConstants.FUND_ALLOWED_REQUEST_PARAMS_UPDATE_ACTIVE);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.nameParamName, element)) {
			final String name = this.fromApiJsonHelper.extractStringNamed(
					SavingsGroupAPIConstants.nameParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.nameParamName)
					.value(name).notBlank().notExceedingLengthOf(50);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.annualNominalInterestRateParamName,
				element)) {
			final BigDecimal annualNominalInterestRate = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.annualNominalInterestRateParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.annualNominalInterestRateParamName)
					.value(annualNominalInterestRate).notNull()
					.positiveAmount();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.chargesParamName, element)) {
			final JsonArray charges = this.fromApiJsonHelper
					.extractJsonArrayNamed(
							SavingsGroupAPIConstants.chargesParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.chargesParamName)
					.value(charges).notBlank().jsonArrayNotEmpty();
			if (charges != null) {
				for (int i = 0; i < charges.size(); i++) {
					final JsonElement charge = charges.get(i);

					validateChargeExisting(baseDataValidator, element, i,
							charge);
				}
			}
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			//
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
	}

	private void validateNewChargeDef(
			final DataValidatorBuilder baseDataValidator, int index,
			final JsonElement charge) {
		this.fromApiJsonHelper.checkForUnsupportedParameters(
				charge.getAsJsonObject(),
				SavingsGroupAPIConstants.CHARGES_ALLOWED_REQUEST_PARAMS);
		final Integer chargeAppliesToEnum = this.fromApiJsonHelper
				.extractIntegerNamed(
						SavingsGroupAPIConstants.chargeAppliesToIdParamName,
						charge, Locale.getDefault());
		baseDataValidator
				.reset()
				.parameter(SavingsGroupAPIConstants.chargeAppliesToIdParamName)
				.parameterAtIndexArray(
						SavingsGroupAPIConstants.chargeAppliesToIdParamName,
						index + 1).value(chargeAppliesToEnum).notNull()
				.isOneOfTheseValues(1, 101);

		if (chargeAppliesToEnum != null && chargeAppliesToEnum == 1) {
			final Integer chargeTimeEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.chargeTimeIdParamName,
							charge, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.chargeTimeIdParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.chargeTimeIdParamName,
							index + 1).value(chargeTimeEnum).notNull()
					.isOneOfTheseValues(1, 8, 9);
			final Integer chargeCalculationEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.chargeCalculationIdParamName,
							charge, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.chargeCalculationIdParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.chargeCalculationIdParamName,
							index + 1).value(chargeCalculationEnum).notNull()
					.isOneOfTheseValues(1, 2, 3, 4, 5);
		} else if (chargeAppliesToEnum != null && chargeAppliesToEnum == 101) {
			final Integer chargeTimeEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.chargeTimeIdParamName,
							charge, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.chargeTimeIdParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.chargeTimeIdParamName,
							index + 1).value(chargeTimeEnum).notNull()
					.isOneOfTheseValues(101, 102);
			final Integer chargeCalculationEnum = this.fromApiJsonHelper
					.extractIntegerNamed(
							SavingsGroupAPIConstants.chargeCalculationIdParamName,
							charge, Locale.getDefault());
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.chargeCalculationIdParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.chargeCalculationIdParamName,
							index + 1).value(chargeCalculationEnum).notNull()
					.isOneOfTheseValues(1, 2);
		}

		final BigDecimal amount = this.fromApiJsonHelper
				.extractBigDecimalWithLocaleNamed(
						SavingsGroupAPIConstants.amountParamName, charge);
		baseDataValidator
				.reset()
				.parameter(SavingsGroupAPIConstants.amountParamName)
				.parameterAtIndexArray(
						SavingsGroupAPIConstants.amountParamName, index + 1)
				.value(amount).notNull().positiveAmount();

		final Boolean isPenalty = this.fromApiJsonHelper.extractBooleanNamed(
				SavingsGroupAPIConstants.penaltyParamName, charge);
		if (isPenalty == null) {
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.penaltyParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.penaltyParamName,
							index + 1).value(isPenalty)
					.trueOrFalseRequired(false);
		}
		final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(
				SavingsGroupAPIConstants.activeParamName, charge);
		if (isActive == null) {
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.activeParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.activeParamName, index + 1)
					.value(isActive).trueOrFalseRequired(false);
		}
	}

	private void validateChargeExisting(
			final DataValidatorBuilder baseDataValidator,
			final JsonElement element, int index, final JsonElement charge) {
		this.fromApiJsonHelper
				.checkForUnsupportedParameters(
						charge.getAsJsonObject(),
						SavingsGroupAPIConstants.CHARGES_ALLOWED_REQUEST_PARAMS_FOR_UPDATE_EXISTING);

		final Integer chargeId = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed("id", charge);
		baseDataValidator.reset().parameter("id")
				.parameterAtIndexArray("id", index + 1).value(chargeId)
				.notNull().integerGreaterThanZero();

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.amountParamName, element)) {
			final BigDecimal amount = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.amountParamName, charge);
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.amountParamName)
					.parameterAtIndexArray(
							SavingsGroupAPIConstants.amountParamName, index + 1)
					.value(amount).notNull().positiveAmount();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.activeParamName, element)) {
			final Boolean isActive = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.activeParamName, charge);
			if (isActive == null) {
				baseDataValidator
						.reset()
						.parameter(SavingsGroupAPIConstants.activeParamName)
						.parameterAtIndexArray(
								SavingsGroupAPIConstants.activeParamName,
								index + 1).value(isActive)
						.trueOrFalseRequired(false);
			}
		}
	}

	private void validateMinMaxNumberOfRepayments(
			final DataValidatorBuilder baseDataValidator,
			final JsonElement element) {
		final Integer numberOfRepayments = this.fromApiJsonHelper
				.extractIntegerWithLocaleNamed(
						SavingsGroupAPIConstants.numberOfRepaymentsParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(SavingsGroupAPIConstants.numberOfRepaymentsParamName)
				.value(numberOfRepayments).notNull().integerGreaterThanZero();

		final String minNumberOfRepaymentsParameterName = SavingsGroupAPIConstants.minNumberOfRepaymentsParamName;
		Integer minNumberOfRepayments = null;
		if (this.fromApiJsonHelper.parameterExists(
				minNumberOfRepaymentsParameterName, element)) {
			minNumberOfRepayments = this.fromApiJsonHelper
					.extractIntegerWithLocaleNamed(
							minNumberOfRepaymentsParameterName, element);
			baseDataValidator.reset()
					.parameter(minNumberOfRepaymentsParameterName)
					.value(minNumberOfRepayments).ignoreIfNull()
					.integerGreaterThanZero();
		}

		final String maxNumberOfRepaymentsParameterName = SavingsGroupAPIConstants.maxNumberOfRepaymentsParamName;
		Integer maxNumberOfRepayments = null;
		if (this.fromApiJsonHelper.parameterExists(
				maxNumberOfRepaymentsParameterName, element)) {
			maxNumberOfRepayments = this.fromApiJsonHelper
					.extractIntegerWithLocaleNamed(
							maxNumberOfRepaymentsParameterName, element);
			baseDataValidator.reset()
					.parameter(maxNumberOfRepaymentsParameterName)
					.value(maxNumberOfRepayments).ignoreIfNull()
					.integerGreaterThanZero();
		}

		if (maxNumberOfRepayments != null
				&& maxNumberOfRepayments.compareTo(0) == 1) {
			if (minNumberOfRepayments != null
					&& minNumberOfRepayments.compareTo(0) == 1) {
				baseDataValidator.reset()
						.parameter(maxNumberOfRepaymentsParameterName)
						.value(maxNumberOfRepayments)
						.notLessThanMin(minNumberOfRepayments);
				if (minNumberOfRepayments.compareTo(maxNumberOfRepayments) <= 0) {
					baseDataValidator
							.reset()
							.parameter(
									SavingsGroupAPIConstants.numberOfRepaymentsParamName)
							.value(numberOfRepayments)
							.inMinMaxRange(minNumberOfRepayments,
									maxNumberOfRepayments);
				}
			} else {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.numberOfRepaymentsParamName)
						.value(numberOfRepayments)
						.notGreaterThanMax(maxNumberOfRepayments);
			}
		} else if (minNumberOfRepayments != null
				&& minNumberOfRepayments.compareTo(0) == 1) {
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.numberOfRepaymentsParamName)
					.value(numberOfRepayments)
					.notLessThanMin(minNumberOfRepayments);
		}
	}

	private void validateLoanLimits(
			final DataValidatorBuilder baseDataValidator,
			final JsonElement element, final Boolean isLoanLimitBasedOnSavings) {
		if (isLoanLimitBasedOnSavings) {
			final Integer loanLimitFactor = this.fromApiJsonHelper
					.extractIntegerSansLocaleNamed(
							SavingsGroupAPIConstants.loanLimitFactorParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.loanLimitFactorParamName)
					.value(loanLimitFactor).notNull().integerGreaterThanZero();
		} else {
			final BigDecimal loanLimitAmount = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.loanLimitAmountParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.loanLimitAmountParamName)
					.value(loanLimitAmount).notNull().positiveAmount();
		}
	}

}
