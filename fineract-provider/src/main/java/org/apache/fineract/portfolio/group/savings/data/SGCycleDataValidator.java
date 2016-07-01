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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SGCycleDataValidator {

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public SGCycleDataValidator(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreateCycle(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.CYCLE_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		final String code = this.fromApiJsonHelper.extractStringNamed(
				SavingsGroupAPIConstants.currencyCodeParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.currencyCodeParamName)
				.value(code).notBlank().notExceedingLengthOf(3);

		final Integer digitsAfterDecimal = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed(
						SavingsGroupAPIConstants.currencyDigitsParamName,
						element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.currencyDigitsParamName)
				.value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

		final Integer inMultiplesOf = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed(
						SavingsGroupAPIConstants.currencyMultiplesOfParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.currencyMultiplesOfParamName)
				.value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();

		final LocalDate expectedStartDate = this.fromApiJsonHelper
				.extractLocalDateNamed(
						SavingsGroupAPIConstants.startDateParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.startDateParamName)
				.value(expectedStartDate).notNull();

		final LocalDate expectedEndDate = this.fromApiJsonHelper
				.extractLocalDateNamed(
						SavingsGroupAPIConstants.endDateParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.endDateParamName)
				.value(expectedEndDate).notNull();

		final Boolean isShareBased = this.fromApiJsonHelper
				.extractBooleanNamed(
						SavingsGroupAPIConstants.isShareBasedParamName, element);
		if (isShareBased == null) {
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.isShareBasedParamName)
					.value(isShareBased).trueOrFalseRequired(false);
		}

		if(null != isShareBased
				&& isShareBased){
			final BigDecimal unitPriceOfShare = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.unitPriceOfShareParamName,
							element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.unitPriceOfShareParamName)
					.value(unitPriceOfShare).notNull().positiveAmount();
		}

		final Boolean isClientAdditionsAllowedInActiveCycle = this.fromApiJsonHelper
				.extractBooleanNamed(
						SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName,
						element);
		if (isClientAdditionsAllowedInActiveCycle == null) {
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName)
					.value(isClientAdditionsAllowedInActiveCycle)
					.trueOrFalseRequired(false);
		}

		final Boolean isClientExitAllowedInActiveCycle = this.fromApiJsonHelper
				.extractBooleanNamed(
						SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName,
						element);
		if (isClientExitAllowedInActiveCycle == null) {
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName)
					.value(isClientExitAllowedInActiveCycle)
					.trueOrFalseRequired(false);
		}

		final Boolean doesIndividualClientExitForfeitGains = this.fromApiJsonHelper
				.extractBooleanNamed(
						SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName,
						element);
		if (doesIndividualClientExitForfeitGains == null) {
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName)
					.value(doesIndividualClientExitForfeitGains)
					.trueOrFalseRequired(false);
		}

		final Integer depositsPaymentStrategyEnum = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed(
						SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName,
						element);
		baseDataValidator
				.reset()
				.parameter(
						SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName)
				.value(depositsPaymentStrategyEnum).notNull()
				.inMinMaxRange(1, 6);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForActivateCycle(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.CYCLE_ACTIVATE_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		final LocalDate expectedStartDate = this.fromApiJsonHelper
				.extractLocalDateNamed(
						SavingsGroupAPIConstants.startDateParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.startDateParamName)
				.value(expectedStartDate).notNull();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForShareOutCloseCycle(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.CYCLE_SHAREOUTCLOSE_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		final LocalDate endDate = this.fromApiJsonHelper
				.extractLocalDateNamed(
						SavingsGroupAPIConstants.endDateParamName, element);
		baseDataValidator.reset()
				.parameter(SavingsGroupAPIConstants.endDateParamName)
				.value(endDate).notNull();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForUpdateCycle(JsonCommand command) {
		final String json = command.json();

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				SavingsGroupAPIConstants.CYCLE_ALLOWED_REQUEST_PARAMS);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors)
				.resource(SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		final JsonElement element = command.parsedJson();

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.currencyCodeParamName, element)) {
			final String code = this.fromApiJsonHelper.extractStringNamed(
					SavingsGroupAPIConstants.currencyCodeParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.currencyCodeParamName)
					.value(code).notBlank().notExceedingLengthOf(3);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.currencyDigitsParamName, element)) {
			final Integer digitsAfterDecimal = this.fromApiJsonHelper
					.extractIntegerSansLocaleNamed(
							SavingsGroupAPIConstants.currencyDigitsParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(SavingsGroupAPIConstants.currencyDigitsParamName)
					.value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.currencyMultiplesOfParamName, element)) {
			final Integer inMultiplesOf = this.fromApiJsonHelper
					.extractIntegerSansLocaleNamed(
							SavingsGroupAPIConstants.currencyMultiplesOfParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.currencyMultiplesOfParamName)
					.value(inMultiplesOf).ignoreIfNull().integerZeroOrGreater();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.startDateParamName, element)) {
			final LocalDate expectedStartDate = this.fromApiJsonHelper
					.extractLocalDateNamed(
							SavingsGroupAPIConstants.startDateParamName,
							element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.startDateParamName)
					.value(expectedStartDate).notNull();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.endDateParamName, element)) {
			final LocalDate expectedEndDate = this.fromApiJsonHelper
					.extractLocalDateNamed(
							SavingsGroupAPIConstants.endDateParamName, element);
			baseDataValidator.reset()
					.parameter(SavingsGroupAPIConstants.endDateParamName)
					.value(expectedEndDate).notNull();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.unitPriceOfShareParamName, element)) {
			final BigDecimal unitPriceOfShare = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(
							SavingsGroupAPIConstants.unitPriceOfShareParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.unitPriceOfShareParamName)
					.value(unitPriceOfShare).ignoreIfNull().positiveAmount();
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.isShareBasedParamName, element)) {
			final Boolean isShareBased = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.isShareBasedParamName,
							element);
			if (isShareBased == null) {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.isShareBasedParamName)
						.value(isShareBased).trueOrFalseRequired(false);
			}
		}

		if (this.fromApiJsonHelper
				.parameterExists(
						SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName,
						element)) {
			final Boolean isClientAdditionsAllowedInActiveCycle = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName,
							element);
			if (isClientAdditionsAllowedInActiveCycle == null) {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName)
						.value(isClientAdditionsAllowedInActiveCycle)
						.trueOrFalseRequired(false);
			}
		}

		if (this.fromApiJsonHelper
				.parameterExists(
						SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName,
						element)) {
			final Boolean isClientExitAllowedInActiveCycle = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName,
							element);
			if (isClientExitAllowedInActiveCycle == null) {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName)
						.value(isClientExitAllowedInActiveCycle)
						.trueOrFalseRequired(false);
			}
		}

		if (this.fromApiJsonHelper
				.parameterExists(
						SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName,
						element)) {
			final Boolean doesIndividualClientExitForfeitGains = this.fromApiJsonHelper
					.extractBooleanNamed(
							SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName,
							element);
			if (doesIndividualClientExitForfeitGains == null) {
				baseDataValidator
						.reset()
						.parameter(
								SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName)
						.value(doesIndividualClientExitForfeitGains)
						.trueOrFalseRequired(false);
			}
		}

		if (this.fromApiJsonHelper.parameterExists(
				SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName,
				element)) {
			final Integer depositsPaymentStrategyEnum = this.fromApiJsonHelper
					.extractIntegerSansLocaleNamed(
							SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName,
							element);
			baseDataValidator
					.reset()
					.parameter(
							SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName)
					.value(depositsPaymentStrategyEnum).notNull()
					.inMinMaxRange(1, 6);
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
}
