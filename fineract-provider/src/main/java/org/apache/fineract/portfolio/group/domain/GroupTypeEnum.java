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
package org.apache.fineract.portfolio.group.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum GroupTypeEnum {

	REGULAR(1, "groupType.Regular", "Regular Group"), 
	SAVINGS(2, "groupType.Savings", "Savings Group");

	private final Integer value;
	private final String code;
	private final String description;

	private GroupTypeEnum(final Integer value, final String code,
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

	public static GroupTypeEnum fromInt(final Integer value) {
		GroupTypeEnum retVal;

		switch (value) {
		case 2:
			retVal = GroupTypeEnum.SAVINGS;
			break;
		default:
			retVal = GroupTypeEnum.REGULAR;
			break;
		}
		return retVal;
	}

	public boolean hasStateOf(final GroupTypeEnum ref) {
		if (null == ref)
			return false;

		return this.value.equals(ref.getValue());
	}

	public static EnumOptionData getEnumOptionData(final Integer value) {
		GroupTypeEnum type = GroupTypeEnum.fromInt(value);
		return new EnumOptionData(type.value.longValue(), type.code,
				type.description);
	}

	public static List<EnumOptionData> getGroupTypeOptions() {
		List<EnumOptionData> retVal = new ArrayList<>();
		for (GroupTypeEnum option : GroupTypeEnum.values()) {
			retVal.add(getEnumOptionData(option.value));
		}
		return retVal;
	}
}
