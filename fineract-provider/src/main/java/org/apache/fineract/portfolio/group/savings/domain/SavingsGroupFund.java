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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Entity
@Table(name = "m_savings_group_funds")
public class SavingsGroupFund extends AbstractPersistable<Long> {

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@Column(name = "minimum_deposit_per_meeting", nullable = false)
	private BigDecimal minimumDepositPerMeeting;

	@Column(name = "maximum_deposit_per_meeting", nullable = false)
	private BigDecimal maximumDepositPerMeeting;

	@ManyToOne
	@JoinColumn(name = "loan_product_id", nullable = true)
	private LoanProduct loanProduct;

	@ManyToOne
	@JoinColumn(name = "cycle_id", nullable = false)
	private SavingsGroupCycle cycle;

	@Column(name = "fund_status", nullable = false)
	private Integer fundStatusEnum;

	@Column(name = "total_cash_in_hand", nullable = true)
	private BigDecimal totalCashInHand = BigDecimal.ZERO;

	@Column(name = "total_cash_in_bank", nullable = true)
	private BigDecimal totalCashInBank = BigDecimal.ZERO;

	@Column(name = "total_deposits", nullable = true)
	private BigDecimal totalDeposits = BigDecimal.ZERO;

	@Column(name = "total_loan_portfolio", nullable = true)
	private BigDecimal totalLoanPortfolio = BigDecimal.ZERO;

	@Column(name = "total_fee_collected", nullable = true)
	private BigDecimal totalFeeCollected = BigDecimal.ZERO;

	@Column(name = "total_expenses", nullable = true)
	private BigDecimal totalExpenses = BigDecimal.ZERO;

	@Column(name = "total_income", nullable = true)
	private BigDecimal totalIncome = BigDecimal.ZERO;

	@Column(name = "is_loan_limit_based_on_savings", nullable = false)
	private Boolean isLoanLimitBasedOnSavings;

	@Column(name = "loan_limit_amount", nullable = true)
	private BigDecimal loanLimitAmount;

	@Column(name = "loan_limit_factor", nullable = true)
	private Integer loanLimitFactor;

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "fund", orphanRemoval = true, fetch=FetchType.EAGER)
	private SavingsGroupLoanProductDetail loanProductDetails;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "fund", orphanRemoval = true, fetch=FetchType.EAGER)
	private Set<SavingsGroupCharge> charges;

	protected SavingsGroupFund() {

	}

	public static SavingsGroupFund createWith(
			final JsonCommand command,
			final Group group,
			final SavingsGroupCycle cycle,
			final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy) {
		SavingsGroupFund newfund = new SavingsGroupFund();
		newfund.name = command
				.stringValueOfParameterNamed(SavingsGroupAPIConstants.nameParamName);
		newfund.group = group;
		newfund.minimumDepositPerMeeting = command
				.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.minimumDepositPerMeetingParamName);
		newfund.maximumDepositPerMeeting = command
				.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.maximumDepositPerMeetingParamName);
		newfund.cycle = cycle;
		newfund.fundStatusEnum = SavingsGroupFundStatusEnum.ACTIVE.getValue();
		newfund.isLoanLimitBasedOnSavings = command
				.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName);
		newfund.loanLimitAmount = command
				.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.loanLimitAmountParamName);
		newfund.loanLimitFactor = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.loanLimitFactorParamName);
		newfund.loanProductDetails = SavingsGroupLoanProductDetail.createWith(
				command, loanTransactionProcessingStrategy, newfund);
		newfund.charges = SavingsGroupCharge.createWith(command, newfund);
		return newfund;
	}

	public void updateLoanProduct(final LoanProduct loanProduct) {
		this.loanProduct = loanProduct;
	}

	public SavingsGroupCycle getCycle() {
		return this.cycle;
	}

	public Map<String, Object> update(final JsonCommand command) {
		Map<String, Object> changes = new HashMap<>();
		if (command.isChangeInStringParameterNamed(
				SavingsGroupAPIConstants.nameParamName, this.name)) {
			this.name = command
					.stringValueOfParameterNamed(SavingsGroupAPIConstants.nameParamName);
			changes.put(SavingsGroupAPIConstants.nameParamName, this.name);
		}
		if (command.isChangeInBigDecimalParameterNamed(
				SavingsGroupAPIConstants.minimumDepositPerMeetingParamName,
				this.minimumDepositPerMeeting)) {
			this.minimumDepositPerMeeting = command
					.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.minimumDepositPerMeetingParamName);
			changes.put(
					SavingsGroupAPIConstants.minimumDepositPerMeetingParamName,
					this.minimumDepositPerMeeting);
		}
		if (command.isChangeInBigDecimalParameterNamed(
				SavingsGroupAPIConstants.maximumDepositPerMeetingParamName,
				this.maximumDepositPerMeeting)) {
			this.maximumDepositPerMeeting = command
					.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.maximumDepositPerMeetingParamName);
			changes.put(
					SavingsGroupAPIConstants.maximumDepositPerMeetingParamName,
					this.maximumDepositPerMeeting);
		}
		if (command.isChangeInBooleanParameterNamed(
				SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName,
				this.isLoanLimitBasedOnSavings)) {
			this.isLoanLimitBasedOnSavings = command
					.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName);
			changes.put(
					SavingsGroupAPIConstants.isLoanLimitBasedOnSavingsParamName,
					this.isLoanLimitBasedOnSavings);
		}
		if (this.isLoanLimitBasedOnSavings) {
			this.loanLimitFactor = command
					.integerValueOfParameterNamed(SavingsGroupAPIConstants.loanLimitFactorParamName);
			changes.put(SavingsGroupAPIConstants.loanLimitFactorParamName,
					this.loanLimitFactor);
			this.loanLimitAmount = null;
		} else {
			this.loanLimitAmount = command
					.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.loanLimitAmountParamName);
			changes.put(SavingsGroupAPIConstants.loanLimitAmountParamName,
					this.loanLimitAmount);
			this.loanLimitFactor = null;
		}
		this.loanProductDetails.update(changes, command);

		if (command.parameterExists(SavingsGroupAPIConstants.chargesParamName)) {
			Set<Object> chargesChanges = new HashSet<>();
			JsonArray arrayOfParameterNamed = command
					.arrayOfParameterNamed(SavingsGroupAPIConstants.chargesParamName);
			for (final JsonElement charge : arrayOfParameterNamed) {
				final JsonObject chargeObject = charge.getAsJsonObject();
				final JsonElement amountParam = chargeObject.get(
						SavingsGroupAPIConstants.amountParamName); 
				final BigDecimal amount = amountParam == null? null
						: amountParam.getAsBigDecimal();
				final JsonElement isActiveParam = chargeObject.get(
						SavingsGroupAPIConstants.activeParamName); 
				final Boolean isActive = isActiveParam == null? null
						:isActiveParam.getAsBoolean();
				if (chargeObject.has("id")) {
					final Long chargeId = chargeObject.get("id").getAsLong();
					SavingsGroupCharge chargeToUpdate = getExistingCharge(chargeId);
					if (chargeToUpdate != null) {
						Map<String, Object> chargeupdates = chargeToUpdate
								.updateWith(amount, isActive);
						if (chargeupdates.size() > 0) {
							chargeupdates.put("id", chargeId);
							chargesChanges.add(chargeupdates);
						}
					}
				} else {
					final Integer chargeAppliesToEnum = chargeObject.get(
							SavingsGroupAPIConstants.chargeAppliesToIdParamName)
							.getAsInt();
					final Integer chargeTimeEnum = chargeObject.get(
							SavingsGroupAPIConstants.chargeTimeIdParamName)
							.getAsInt();
					final Integer chargeCalculationEnum = chargeObject.get(
							SavingsGroupAPIConstants.chargeCalculationIdParamName)
							.getAsInt();
					final Boolean isPenalty = chargeObject.get(
							SavingsGroupAPIConstants.penaltyParamName)
							.getAsBoolean();
					SavingsGroupCharge newCharge = SavingsGroupCharge
							.createWith(this, chargeAppliesToEnum, chargeTimeEnum,
									chargeCalculationEnum, amount, isPenalty,
									isActive);
					this.charges.add(newCharge);
					Map<String, Object> newChargeValues = new HashMap<>();
					newChargeValues
							.put(SavingsGroupAPIConstants.chargeAppliesToIdParamName,
									chargeAppliesToEnum);
					newChargeValues.put(
							SavingsGroupAPIConstants.chargeTimeIdParamName,
							chargeTimeEnum);
					newChargeValues
							.put(SavingsGroupAPIConstants.chargeCalculationIdParamName,
									chargeCalculationEnum);
					newChargeValues.put(
							SavingsGroupAPIConstants.amountParamName, amount);
					newChargeValues.put(
							SavingsGroupAPIConstants.penaltyParamName,
							isPenalty);
					newChargeValues.put(
							SavingsGroupAPIConstants.activeParamName, isActive);
					chargesChanges.add(newChargeValues);
				}
			}
			changes.put(SavingsGroupAPIConstants.chargesParamName,
					chargesChanges);
		}
		return changes;
	}

	private SavingsGroupCharge getExistingCharge(final Long chargeId) {
		if (this.charges != null && this.charges.size() > 0) {
			for (SavingsGroupCharge savingsGroupCharge : charges) {
				if (savingsGroupCharge.getId().equals(chargeId)) {
					return savingsGroupCharge;
				}
			}
		}
		return null;
	}

	public Map<String, Object> update(
			final JsonCommand command,
			final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy) {
		Map<String, Object> changes = update(command);
		final Long currentLoanTransactionProcessingStrategyId = this.loanProductDetails
				.getLoanTransactionProcessingStrategy().getId();
		final Long newLoanTransactionProcessingStrategy = loanTransactionProcessingStrategy
				.getId();
		if (!currentLoanTransactionProcessingStrategyId
				.equals(newLoanTransactionProcessingStrategy)) {
			this.loanProductDetails
					.setLoanTransactionProcessingStrategy(loanTransactionProcessingStrategy);
			changes.put(
					SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName,
					newLoanTransactionProcessingStrategy);
		}
		return changes;
	}

	public Group getGroup() {
		return this.group;
	}

	public Map<String, Object> deleteFund() {
		Map<String, Object> changes = new HashMap<>();
		this.fundStatusEnum = SavingsGroupFundStatusEnum.INACTIVE.getValue();
		changes.put(SavingsGroupAPIConstants.fundStatusParamName,
				SavingsGroupFundStatusEnum
						.getEnumOptionData(this.fundStatusEnum));
		return changes;
	}

	public Integer getStatus() {
		return this.fundStatusEnum;
	}

	public SavingsGroupFund copy(final SavingsGroupCycle destCycle) {
		SavingsGroupFund newfund = new SavingsGroupFund();
		newfund.name = this.name;
		newfund.group = this.group;
		newfund.minimumDepositPerMeeting = this.minimumDepositPerMeeting;
		newfund.maximumDepositPerMeeting = this.maximumDepositPerMeeting;
		newfund.cycle = destCycle;
		newfund.fundStatusEnum = SavingsGroupFundStatusEnum.ACTIVE.getValue();
		newfund.isLoanLimitBasedOnSavings = this.isLoanLimitBasedOnSavings;
		newfund.loanLimitAmount = this.loanLimitAmount;
		newfund.loanLimitFactor = this.loanLimitFactor;
		newfund.loanProductDetails = this.loanProductDetails.copy(newfund);
		Set<SavingsGroupCharge> newCharges = null;
		if (this.charges != null && this.charges.size() > 0) {
			newCharges = new HashSet<>();
			for (SavingsGroupCharge charge : this.charges) {
				if (charge.isActive()) {
					newCharges.add(charge.copy(newfund));
				}
			}
		}
		newfund.charges = newCharges;
		return newfund;
	}

}
