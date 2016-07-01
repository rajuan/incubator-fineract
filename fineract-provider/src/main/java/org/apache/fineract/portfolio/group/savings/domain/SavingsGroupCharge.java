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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Entity
@Table(name = "m_savings_group_charges")
public class SavingsGroupCharge extends AbstractPersistable<Long> {

	@ManyToOne
	@JoinColumn(name = "fund_id", nullable = false)
	private SavingsGroupFund fund;

	@Column(name = "charge_applies_to_enum", nullable = true)
	private Integer chargeAppliesToEnum;

	@Column(name = "charge_time_enum", nullable = true)
	private Integer chargeTimeEnum;

	@Column(name = "charge_calculation_enum", nullable = true)
	private Integer chargeCalculationEnum;

	@Column(name = "amount", nullable = true)
	private BigDecimal amount;

	@Column(name = "is_penalty", nullable = true)
	private Boolean isPenalty;

	@Column(name = "is_active", nullable = true)
	private Boolean isActive;

	protected SavingsGroupCharge() {

	}

	private SavingsGroupCharge(final SavingsGroupFund fund, final Integer chargeAppliesToEnum,
			final Integer chargeTimeEnum, final Integer chargeCalculationEnum,
			final BigDecimal amount, final Boolean isPenalty,
			final Boolean isActive) {
		this.fund = fund;
		this.chargeAppliesToEnum = chargeAppliesToEnum;
		this.chargeTimeEnum = chargeTimeEnum;
		this.chargeCalculationEnum = chargeCalculationEnum;
		this.amount = amount;
		this.isPenalty = isPenalty;
		this.isActive = isActive;
	}

	public static Set<SavingsGroupCharge> createWith(final JsonCommand command, 
			final SavingsGroupFund fund) {
		if (!command.parameterExists(SavingsGroupAPIConstants.chargesParamName)) {
			return null;
		}
		Set<SavingsGroupCharge> charges = new HashSet<>();
		JsonArray arrayOfParameterNamed = command
				.arrayOfParameterNamed(SavingsGroupAPIConstants.chargesParamName);
		for (final JsonElement charge : arrayOfParameterNamed) {
			final JsonObject chargeObject = charge.getAsJsonObject();
			final Integer chargeAppliesToEnum = chargeObject.get(
					SavingsGroupAPIConstants.chargeAppliesToIdParamName)
					.getAsInt();
			final Integer chargeTimeEnum = chargeObject.get(
					SavingsGroupAPIConstants.chargeTimeIdParamName).getAsInt();
			final Integer chargeCalculationEnum = chargeObject.get(
					SavingsGroupAPIConstants.chargeCalculationIdParamName)
					.getAsInt();
			final BigDecimal amount = chargeObject.get(
					SavingsGroupAPIConstants.amountParamName).getAsBigDecimal();
			final Boolean isPenalty = chargeObject.get(
					SavingsGroupAPIConstants.penaltyParamName).getAsBoolean();
			final Boolean isActive = chargeObject.get(
					SavingsGroupAPIConstants.activeParamName).getAsBoolean();
			charges.add(new SavingsGroupCharge(fund, chargeAppliesToEnum,
					chargeTimeEnum, chargeCalculationEnum, amount, isPenalty,
					isActive));
		}
		return charges;
	}

	public Map<String, Object> updateWith(final BigDecimal amount,
			final Boolean isActive) {
		Map<String, Object> changes = new HashMap<>();
		if (null != amount && !this.amount.equals(amount)) {
			this.amount = amount;
			changes.put(SavingsGroupAPIConstants.amountParamName, this.amount);
		}
		if (null != isActive && !this.isActive.equals(isActive)) {
			this.isActive = isActive;
			changes.put(SavingsGroupAPIConstants.activeParamName, this.isActive);
		}
		if(changes.size()>0){
			changes.put("id", this.getId());
		}
		return changes;
	}

	public static SavingsGroupCharge createWith(final SavingsGroupFund fund,
			final Integer chargeAppliesToEnum, final Integer chargeTimeEnum,
			final Integer chargeCalculationEnum, final BigDecimal amount,
			final Boolean isPenalty, final Boolean isActive) {
		return new SavingsGroupCharge(fund, chargeAppliesToEnum, chargeTimeEnum,
				chargeCalculationEnum, amount, isPenalty, isActive);
	}

	public Boolean isActive() {
		return this.isActive;
	}

	public SavingsGroupCharge copy(final SavingsGroupFund fund) {
		return new SavingsGroupCharge(fund, this.chargeAppliesToEnum,
				this.chargeTimeEnum, this.chargeCalculationEnum, this.amount,
				this.isPenalty, this.isActive);
	}
}
