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

public enum SavingsGroupChargeTimeEnum {
	INVALID(0, "chargeTimeType.invalid", "Invalid"), 
	DISBURSEMENT(1, "chargeTimeType.disbursement", "Disbursement"), 
	INSTALMENT_FEE(8, "chargeTimeType.instalmentFee", "Installment"), 
	OVERDUE_INSTALLMENT(9, "chargeTimeType.overdueInstallment", "Overdue"), 
	MEETING_ABSENSE(101, "chargeTimeType.meetingabsense", "Meeting Absense"), 
	PARTIAL_DEPOSIT(102, "chargeTimeType.partialdeposit", "Partial Deposit");

	private final Integer value;
	private final String code;
	private final String description;

	private SavingsGroupChargeTimeEnum(final Integer value, final String code,
			final String description) {
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

	public static SavingsGroupChargeTimeEnum fromInt(final Integer value) {
		SavingsGroupChargeTimeEnum retVal = SavingsGroupChargeTimeEnum.INVALID;
		if (null != value) {
			switch (value) {
			case 1:
				retVal = SavingsGroupChargeTimeEnum.DISBURSEMENT;
				break;
			case 8:
				retVal = SavingsGroupChargeTimeEnum.INSTALMENT_FEE;
				break;
			case 9:
				retVal = SavingsGroupChargeTimeEnum.OVERDUE_INSTALLMENT;
				break;
			case 101:
				retVal = SavingsGroupChargeTimeEnum.MEETING_ABSENSE;
				break;
			case 102:
				retVal = SavingsGroupChargeTimeEnum.PARTIAL_DEPOSIT;
				break;
			default:
				break;
			}
		}
		return retVal;
	}

	public boolean hasStateOf(final SavingsGroupChargeTimeEnum ref) {
		if (null == ref)
			return false;

		return this.value.equals(ref.getValue());
	}

	public static EnumOptionData getEnumOptionData(final Integer value) {
		SavingsGroupChargeTimeEnum type = SavingsGroupChargeTimeEnum
				.fromInt(value);
		return new EnumOptionData(type.value.longValue(), type.code,
				type.description);
	}

	public static List<EnumOptionData> getLoanChargeTimeOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		retVal.add(getEnumOptionData(DISBURSEMENT.value));
		retVal.add(getEnumOptionData(INSTALMENT_FEE.value));
		retVal.add(getEnumOptionData(OVERDUE_INSTALLMENT.value));
		return retVal;
	}

	public static List<EnumOptionData> getGroupChargeTimeOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		retVal.add(getEnumOptionData(MEETING_ABSENSE.value));
		retVal.add(getEnumOptionData(PARTIAL_DEPOSIT.value));
		return retVal;
	}
}
