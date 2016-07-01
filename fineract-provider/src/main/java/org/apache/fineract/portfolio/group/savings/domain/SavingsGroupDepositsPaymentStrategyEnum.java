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

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum SavingsGroupDepositsPaymentStrategyEnum {
	INVALID(0, "savingsGroupDepositsPaymentStrategy.INVALID", "Invalid"), 
	CLD(1, "savingsGroupDepositsPaymentStrategy.CLD", "Charge,Loan,Deposit"), 
	CDL(2, "savingsGroupDepositsPaymentStrategy.CDL", "Charge,Deposit,Loan"), 
	DLC(3, "savingsGroupDepositsPaymentStrategy.DLC", "Deposit,Loan,Charge"), 
	DCL(4, "savingsGroupDepositsPaymentStrategy.DCL", "Deposit,Charge,Loan"), 
	LDC(5, "savingsGroupDepositsPaymentStrategy.LDC", "Loan,Deposit,Charge"), 
	LCD(6, "savingsGroupDepositsPaymentStrategy.LCD", "Loan,Charge,Deposit");

	private final Integer value;
	private final String code;
	private final String description;

	private SavingsGroupDepositsPaymentStrategyEnum(final Integer value,
			final String code, final String description) {
		this.value = value;
		this.code = code;
		this.description = description;
	}

	public Integer getValue() {
		return this.value;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}

	public static SavingsGroupDepositsPaymentStrategyEnum fromInt(
			final Integer value) {
		SavingsGroupDepositsPaymentStrategyEnum retVal = SavingsGroupDepositsPaymentStrategyEnum.INVALID;
		if (null != value) {
			switch (value) {
			case 1:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.CLD;
				break;
			case 2:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.CDL;
				break;
			case 3:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.DLC;
				break;
			case 4:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.DCL;
				break;
			case 5:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.LDC;
				break;
			case 6:
				retVal = SavingsGroupDepositsPaymentStrategyEnum.LCD;
				break;
			default:
				break;
			}
		}
		return retVal;
	}

	public boolean hasStateOf(final SavingsGroupDepositsPaymentStrategyEnum ref) {
		if (null == ref)
			return false;

		return this.value.equals(ref.getValue());
	}

	public static EnumOptionData getEnumOptionData(final Integer value) {
		SavingsGroupDepositsPaymentStrategyEnum type = SavingsGroupDepositsPaymentStrategyEnum
				.fromInt(value);
		return new EnumOptionData(type.value.longValue(), type.code,
				type.description);
	}

	public static List<EnumOptionData> getSavingsGroupDepositsPaymentStrategyOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		for (SavingsGroupDepositsPaymentStrategyEnum option : SavingsGroupDepositsPaymentStrategyEnum
				.values()) {
			if(option.hasStateOf(INVALID)){
				continue;
			}
			retVal.add(getEnumOptionData(option.value));
		}
		return retVal;
	}
}
