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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.joda.time.LocalDate;

public class SavingsGroupCycleData {
	private final Long id;
	private final Long cycleNumber;
	private final EnumOptionData status;
	private final CurrencyData currency;
	@SuppressWarnings("unused")
	private final LocalDate expectedStartDate;
	@SuppressWarnings("unused")
	private final LocalDate actualStartDate;
	@SuppressWarnings("unused")
	private final LocalDate expectedEndDate;
	private final LocalDate actualEndDate;
	@SuppressWarnings("unused")
	private final Integer expectedNumOfMeetings;
	@SuppressWarnings("unused")
	private final Integer numOfMeetingsCompleted;
	@SuppressWarnings("unused")
	private final Integer numOfMeetingsPending;
	private final Boolean isShareBased;
	private final BigDecimal unitPriceOfShare;
	private final Long shareProductId;
	private final Boolean isClientAdditionsAllowedInActiveCycle;
	private final Boolean isClientExitAllowedInActiveCycle;
	private final Boolean doesIndividualClientExitForfeitGains;
	private final EnumOptionData depositsPaymentStrategy;

	@SuppressWarnings("unused")
	private final Collection<CurrencyData> currencyOptions;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> depositsPaymentStrategyOptions;

	private SavingsGroupCycleData(final Long id, final Long cycleNumber,
			final EnumOptionData status, final CurrencyData currency,
			final LocalDate expectedStartDate, final LocalDate actualStartDate,
			final LocalDate expectedEndDate, final LocalDate actualEndDate,
			final Integer expectedNumOfMeetings,
			final Integer numOfMeetingsCompleted,
			final Integer numOfMeetingsPending, final Boolean isShareBased,
			final BigDecimal unitPriceOfShare, final Long shareProductId,
			final Boolean isClientAdditionsAllowedInActiveCycle,
			final Boolean isClientExitAllowedInActiveCycle,
			final Boolean doesIndividualClientExitForfeitGains,
			final EnumOptionData depositsPaymentStrategy,
			final Collection<CurrencyData> currencyOptions,
			final Collection<EnumOptionData> depositsPaymentStrategyOptions) {
		this.id = id;
		this.cycleNumber = cycleNumber;
		this.status = status;
		this.currency = currency;
		this.expectedStartDate = expectedStartDate;
		this.actualStartDate = actualStartDate;
		this.expectedEndDate = expectedEndDate;
		this.actualEndDate = actualEndDate;
		this.expectedNumOfMeetings = expectedNumOfMeetings;
		this.numOfMeetingsCompleted = numOfMeetingsCompleted;
		this.numOfMeetingsPending = numOfMeetingsPending;
		this.isShareBased = isShareBased;
		this.unitPriceOfShare = unitPriceOfShare;
		this.shareProductId = shareProductId;
		this.isClientAdditionsAllowedInActiveCycle = isClientAdditionsAllowedInActiveCycle;
		this.isClientExitAllowedInActiveCycle = isClientExitAllowedInActiveCycle;
		this.doesIndividualClientExitForfeitGains = doesIndividualClientExitForfeitGains;
		this.depositsPaymentStrategy = depositsPaymentStrategy;
		this.currencyOptions = currencyOptions;
		this.depositsPaymentStrategyOptions = depositsPaymentStrategyOptions;
	}

	public static SavingsGroupCycleData getTemplate(
			final Collection<CurrencyData> currencyOptions,
			final Collection<EnumOptionData> depositsPaymentStrategyOptions) {
		return new SavingsGroupCycleData(null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null,
				null, null, currencyOptions, depositsPaymentStrategyOptions);
	}

	public static SavingsGroupCycleData getTemplate(
			final SavingsGroupCycleData cycleData,
			final Collection<CurrencyData> currencyOptions,
			final Collection<EnumOptionData> depositsPaymentStrategyOptions) {
		return new SavingsGroupCycleData(null, null, null, cycleData.currency,
				null, null, null, null, null, null, null,
				cycleData.isShareBased, cycleData.unitPriceOfShare, null,
				cycleData.isClientAdditionsAllowedInActiveCycle,
				cycleData.isClientExitAllowedInActiveCycle,
				cycleData.doesIndividualClientExitForfeitGains,
				cycleData.depositsPaymentStrategy, currencyOptions,
				depositsPaymentStrategyOptions);
	}

	public static SavingsGroupCycleData instance(final Long id,
			final Long cycleNumber, final EnumOptionData status,
			final CurrencyData currency, final LocalDate expectedStartDate,
			final LocalDate actualStartDate, final LocalDate expectedEndDate,
			final LocalDate actualEndDate, final Integer expectedNumOfMeetings,
			final Integer numOfMeetingsCompleted,
			final Integer numOfMeetingsPending, final Boolean isShareBased,
			final BigDecimal unitPriceOfShare, final Long shareProductId,
			final Boolean isClientAdditionsAllowedInActiveCycle,
			final Boolean isClientExitAllowedInActiveCycle,
			final Boolean doesIndividualClientExitForfeitGains,
			final EnumOptionData depositsPaymentStrategy) {

		final Collection<CurrencyData> currencyOptions = null;
		final Collection<EnumOptionData> depositsPaymentStrategyOptions = null;
		return new SavingsGroupCycleData(id, cycleNumber, status, currency,
				expectedStartDate, actualStartDate, expectedEndDate,
				actualEndDate, expectedNumOfMeetings, numOfMeetingsCompleted,
				numOfMeetingsPending, isShareBased, unitPriceOfShare,
				shareProductId, isClientAdditionsAllowedInActiveCycle,
				isClientExitAllowedInActiveCycle,
				doesIndividualClientExitForfeitGains, depositsPaymentStrategy,
				currencyOptions, depositsPaymentStrategyOptions);
	}

	public Long getId() {
		return this.id;
	}

	public EnumOptionData getStatus() {
		return this.status;
	}

	public Long getCycleNumber() {
		return this.cycleNumber;
	}

	public Long getShareProductId() {
		return this.shareProductId;
	}

	public LocalDate getEndDate() {
		return this.actualEndDate;
	}
}
