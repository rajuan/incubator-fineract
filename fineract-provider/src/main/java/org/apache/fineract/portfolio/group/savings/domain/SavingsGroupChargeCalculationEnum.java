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

public enum SavingsGroupChargeCalculationEnum {
	INVALID(0, "chargeCalculationType.invalid", "Invalid"), //
	FLAT(1, "chargeCalculationType.flat", "Flat"), //
	PERCENT_OF_AMOUNT(2, "chargeCalculationType.percent.of.amount", "% Amount"), //
	PERCENT_OF_AMOUNT_AND_INTEREST(3,
			"chargeCalculationType.percent.of.amount.and.interest",
			"% Loan Amount + Interest"), //
	PERCENT_OF_INTEREST(4, "chargeCalculationType.percent.of.interest",
			"% Interest"), 
	PERCENT_OF_DISBURSEMENT_AMOUNT(5,
			"chargeCalculationType.percent.of.disbursement.amount",
			"% Disbursement Amount");

	private final Integer value;
	private final String code;
	private final String description;

	private SavingsGroupChargeCalculationEnum(final Integer value,
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

	public static SavingsGroupChargeCalculationEnum fromInt(final Integer value) {
		SavingsGroupChargeCalculationEnum retVal = SavingsGroupChargeCalculationEnum.INVALID;
		if (null != value) {
			switch (value) {
			case 1:
				retVal = SavingsGroupChargeCalculationEnum.FLAT;
				break;
			case 2:
				retVal = SavingsGroupChargeCalculationEnum.PERCENT_OF_AMOUNT;
				break;
			case 3:
				retVal = SavingsGroupChargeCalculationEnum.PERCENT_OF_AMOUNT_AND_INTEREST;
				break;
			case 4:
				retVal = SavingsGroupChargeCalculationEnum.PERCENT_OF_INTEREST;
				break;
			case 5:
				retVal = SavingsGroupChargeCalculationEnum.PERCENT_OF_DISBURSEMENT_AMOUNT;
				break;
			default:
				break;
			}
		}
		return retVal;
	}

	public boolean hasStateOf(final SavingsGroupChargeCalculationEnum ref) {
		if (null == ref)
			return false;

		return this.value.equals(ref.getValue());
	}

	public static EnumOptionData getEnumOptionData(final Integer value) {
		SavingsGroupChargeCalculationEnum type = SavingsGroupChargeCalculationEnum
				.fromInt(value);
		return new EnumOptionData(type.value.longValue(), type.code,
				type.description);
	}

	public static List<EnumOptionData> getLoanChargeCalculationOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		retVal.add(getEnumOptionData(FLAT.value));
		retVal.add(getEnumOptionData(PERCENT_OF_AMOUNT.value));
		retVal.add(getEnumOptionData(PERCENT_OF_AMOUNT_AND_INTEREST.value));
		retVal.add(getEnumOptionData(PERCENT_OF_INTEREST.value));
		retVal.add(getEnumOptionData(PERCENT_OF_DISBURSEMENT_AMOUNT.value));
		return retVal;
	}

	public static List<EnumOptionData> getGroupChargeCalculationOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		retVal.add(getEnumOptionData(FLAT.value));
		retVal.add(getEnumOptionData(PERCENT_OF_AMOUNT.value));
		return retVal;
	}
}
