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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_group_cycle")
public class SavingsGroupCycle extends AbstractPersistable<Long> {

	@ManyToOne
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@Column(name = "cycle_num", nullable = false)
	private Long cycleNumber;

	@Column(name = "status_enum", nullable = false)
	private Integer statusEnum;

	@Embedded
	private MonetaryCurrency currency;

    @Temporal(TemporalType.DATE)
	@Column(name = "expected_start_date", nullable = true)
	private Date expectedStartDate;

    @Temporal(TemporalType.DATE)
	@Column(name = "actual_start_date", nullable = true)
	private Date actualStartDate;

    @Temporal(TemporalType.DATE)
	@Column(name = "expected_end_date", nullable = true)
	private Date expectedEndDate;

    @Temporal(TemporalType.DATE)
	@Column(name = "actual_end_date", nullable = true)
	private Date actualEndDate;

	@Column(name = "expected_num_of_meetings", nullable = true)
	private Integer expectedNumOfMeetings;

	@Column(name = "num_of_meetings_completed", nullable = true)
	private Integer numOfMeetingsCompleted;

	@Column(name = "num_of_meetings_pending", nullable = true)
	private Integer numOfMeetingsPending;

	@Column(name = "is_share_based", nullable = false)
	private Boolean isShareBased;

	@Column(name = "unit_price_of_share", nullable = false)
	private BigDecimal unitPriceOfShare = BigDecimal.ONE;

	@ManyToOne
	@JoinColumn(name = "share_product_id", nullable = true)
	private ShareProduct shareProduct;

	@Column(name = "is_client_additions_allowed_in_active_cycle", nullable = false)
	private Boolean isClientAdditionsAllowedInActiveCycle;

	@Column(name = "is_client_exit_allowed_in_active_cycle", nullable = false)
	private Boolean isClientExitAllowedInActiveCycle;

	@Column(name = "does_individual_client_exit_forfeit_gains", nullable = false)
	private Boolean doesIndividualClientExitForfeitGains;

	@Column(name = "deposits_payment_strategy", nullable = false)
	private Integer depositsPaymentStrategyEnum;

	protected SavingsGroupCycle() {

	}

	private SavingsGroupCycle(final Group group, final Long cycleNumber,
			final Integer statusEnum, final MonetaryCurrency currency,
			final LocalDate expectedStartDate, final LocalDate actualStartDate,
			final LocalDate expectedEndDate, final LocalDate actualEndDate,
			final Integer expectedNumOfMeetings,
			final Integer numOfMeetingsCompleted,
			final Integer numOfMeetingsPending, final Boolean isShareBased,
			final BigDecimal unitPriceOfShare, final ShareProduct shareProduct,
			final Boolean isClientAdditionsAllowedInActiveCycle,
			final Boolean isClientExitAllowedInActiveCycle,
			final Boolean doesIndividualClientExitForfeitGains,
			final Integer depositsPaymentStrategyEnum) {
		this.group = group;
		this.cycleNumber = cycleNumber;
		this.statusEnum = statusEnum;
		this.currency = currency;
		this.expectedStartDate = null == expectedStartDate? null : expectedStartDate.toDate();
		this.actualStartDate = null == actualStartDate? null : actualStartDate.toDate();
		this.expectedEndDate = null == expectedEndDate? null : expectedEndDate.toDate();
		this.actualEndDate = null == actualEndDate? null : actualEndDate.toDate();
		this.expectedNumOfMeetings = expectedNumOfMeetings;
		this.numOfMeetingsCompleted = numOfMeetingsCompleted;
		this.numOfMeetingsPending = numOfMeetingsPending;
		this.isShareBased = isShareBased;
		this.unitPriceOfShare = unitPriceOfShare;
		this.shareProduct = shareProduct;
		this.isClientAdditionsAllowedInActiveCycle = isClientAdditionsAllowedInActiveCycle;
		this.isClientExitAllowedInActiveCycle = isClientExitAllowedInActiveCycle;
		this.doesIndividualClientExitForfeitGains = doesIndividualClientExitForfeitGains;
		this.depositsPaymentStrategyEnum = depositsPaymentStrategyEnum;

	}

	public static SavingsGroupCycle newCycle(final Group group,
			final Long cycleNum, final MonetaryCurrency currency,
			final LocalDate expectedStartDate, final LocalDate actualStartDate,
			final LocalDate expectedEndDate, final LocalDate actualEndDate,
			final Integer expectedNumOfMeetings,
			final Integer numOfMeetingsCompleted,
			final Integer numOfMeetingsPending, final Boolean isShareBased,
			final BigDecimal unitPriceOfShare, final ShareProduct shareProduct,
			final Boolean isClientAdditionsAllowedInActiveCycle,
			final Boolean isClientExitAllowedInActiveCycle,
			final Boolean doesIndividualClientExitForfeitGains,
			final Integer depositsPaymentStrategyEnum) {
		final Integer statusEnum = SavingsGroupCycleStatusEnum.INITIATED
				.getValue();
		return new SavingsGroupCycle(group, cycleNum, statusEnum, currency,
				expectedStartDate, actualStartDate, expectedEndDate,
				actualEndDate, expectedNumOfMeetings, numOfMeetingsCompleted,
				numOfMeetingsPending, isShareBased, unitPriceOfShare,
				shareProduct, isClientAdditionsAllowedInActiveCycle,
				isClientExitAllowedInActiveCycle,
				doesIndividualClientExitForfeitGains,
				depositsPaymentStrategyEnum);
	}

	public Integer getStatusEnum() {
		return this.statusEnum;
	}

	public Map<String, Object> activateCycle(final LocalDate startDate) {
		this.actualStartDate = startDate.toDate();
		this.statusEnum = SavingsGroupCycleStatusEnum.ACTIVE.getValue();

		Map<String, Object> changes = new HashMap<>();
		changes.put(SavingsGroupAPIConstants.actualStartDateParamName,
				startDate);
		changes.put(SavingsGroupAPIConstants.statusParamName,
				SavingsGroupCycleStatusEnum.getEnumOptionData(statusEnum));
		return changes;
	}

	public Map<String, Object> closeCycle(final LocalDate endDate) {
		this.actualEndDate = endDate.toDate();
		this.statusEnum = SavingsGroupCycleStatusEnum.CLOSED.getValue();

		Map<String, Object> changes = new HashMap<>();
		changes.put(SavingsGroupAPIConstants.actualEndDateParamName, endDate);
		changes.put(SavingsGroupAPIConstants.statusParamName,
				SavingsGroupCycleStatusEnum.getEnumOptionData(statusEnum));
		return changes;
	}

	public Long getCycleNumber() {
		return this.cycleNumber;
	}

	public ShareProduct getShareProduct() {
		return this.shareProduct;
	}

	public LocalDate getExpectedStartDate() {
		return LocalDate.fromDateFields(this.expectedStartDate);
	}

	public LocalDate getExpectedEndDate() {
		return LocalDate.fromDateFields(this.expectedEndDate);
	}

	public Map<String, Object> updateCycle(final JsonCommand command,
			final Integer expectedNumOfMeetings) {
		Map<String, Object> changes = new HashMap<>();
		String code = this.currency.getCode();
		Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
		Integer inMultiplesOf = this.currency.getCurrencyInMultiplesOf();
		boolean changeInCurrency = false;
		if (command.isChangeInStringParameterNamed(
				SavingsGroupAPIConstants.currencyCodeParamName,
				this.currency.getCode())) {
			code = command
					.stringValueOfParameterNamed(SavingsGroupAPIConstants.currencyCodeParamName);
			changeInCurrency = true;
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.currencyDigitsParamName,
				this.currency.getDigitsAfterDecimal())) {
			digitsAfterDecimal = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.currencyDigitsParamName);
			changeInCurrency = true;
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.currencyMultiplesOfParamName,
				this.currency.getCurrencyInMultiplesOf())) {
			inMultiplesOf = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.currencyMultiplesOfParamName);
			changeInCurrency = true;
		}
		if (changeInCurrency) {
			this.currency = new MonetaryCurrency(code, digitsAfterDecimal,
					inMultiplesOf);
			changes.put(SavingsGroupAPIConstants.currencyParamName,
					this.currency);
		}
		if (command.isChangeInDateParameterNamed(
				SavingsGroupAPIConstants.startDateParamName,
				this.expectedStartDate)) {
			this.expectedStartDate = command
					.DateValueOfParameterNamed(SavingsGroupAPIConstants.startDateParamName);
			changes.put(SavingsGroupAPIConstants.expectedStartDateParamName,
					this.expectedStartDate);

		}
		if (command
				.isChangeInDateParameterNamed(
						SavingsGroupAPIConstants.endDateParamName,
						this.expectedEndDate)) {
			this.expectedEndDate = command
					.DateValueOfParameterNamed(SavingsGroupAPIConstants.endDateParamName);
			changes.put(SavingsGroupAPIConstants.expectedEndDateParamName,
					this.expectedEndDate);

		}
		if (command.isChangeInBooleanParameterNamed(
				SavingsGroupAPIConstants.isShareBasedParamName,
				this.isShareBased)) {
			this.isShareBased = command
					.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isShareBasedParamName);
			changes.put(SavingsGroupAPIConstants.isShareBasedParamName,
					this.isShareBased);
		}
		if(this.isShareBased){
			if (command.isChangeInBigDecimalParameterNamed(
					SavingsGroupAPIConstants.unitPriceOfShareParamName,
					this.unitPriceOfShare)) {
				this.unitPriceOfShare = command
						.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.unitPriceOfShareParamName);
				changes.put(SavingsGroupAPIConstants.unitPriceOfShareParamName,
						this.unitPriceOfShare);
			}
		}else{
			this.unitPriceOfShare = BigDecimal.ONE;
		}
		if (command
				.isChangeInBooleanParameterNamed(
						SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName,
						this.isClientAdditionsAllowedInActiveCycle)) {
			this.isClientAdditionsAllowedInActiveCycle = command
					.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName);
			changes.put(
					SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName,
					this.isClientAdditionsAllowedInActiveCycle);
		}
		if (command
				.isChangeInBooleanParameterNamed(
						SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName,
						this.isClientExitAllowedInActiveCycle)) {
			this.isClientExitAllowedInActiveCycle = command
					.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName);
			changes.put(
					SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName,
					this.isClientExitAllowedInActiveCycle);
		}
		if (command
				.isChangeInBooleanParameterNamed(
						SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName,
						this.doesIndividualClientExitForfeitGains)) {
			this.doesIndividualClientExitForfeitGains = command
					.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName);
			changes.put(
					SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName,
					this.doesIndividualClientExitForfeitGains);
		}
		if (command.isChangeInIntegerParameterNamed(
				SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName,
				this.depositsPaymentStrategyEnum)) {
			this.depositsPaymentStrategyEnum = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName);
			changes.put(
					SavingsGroupAPIConstants.depositsPaymentStrategyParamName,
					SavingsGroupDepositsPaymentStrategyEnum
							.getEnumOptionData(this.depositsPaymentStrategyEnum));
		}
		if (this.expectedNumOfMeetings != expectedNumOfMeetings) {
			this.expectedNumOfMeetings = expectedNumOfMeetings;
			changes.put(
					SavingsGroupAPIConstants.expectedNumOfMeetingsParamName,
					this.expectedNumOfMeetings);
		}
		return changes;
	}

	public MonetaryCurrency getCurrency() {
		return this.currency;
	}

	public BigDecimal getUnitPriceOfShare() {
		return this.unitPriceOfShare;
	}

	public void setShareProduct(final ShareProduct shareProduct) {
		this.shareProduct = shareProduct;
	}

	public LocalDate getEndDate() {
		return null == this.actualEndDate? 
				LocalDate.fromDateFields(this.expectedEndDate) 
				: LocalDate.fromDateFields(this.actualEndDate);
	}
	
	public LocalDate getStartDate(){
		return null == this.actualStartDate? 
				LocalDate.fromDateFields(this.expectedStartDate) 
				: LocalDate.fromDateFields(this.actualStartDate);
	}
}
