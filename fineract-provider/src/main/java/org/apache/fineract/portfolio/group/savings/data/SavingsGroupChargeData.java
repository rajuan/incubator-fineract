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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class SavingsGroupChargeData {
	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final EnumOptionData chargeAppliesToEnum;
	@SuppressWarnings("unused")
	private final EnumOptionData chargeTimeEnum;
	@SuppressWarnings("unused")
	private final EnumOptionData chargeCalculationEnum;
	@SuppressWarnings("unused")
	private final BigDecimal amount;
	@SuppressWarnings("unused")
	private final Boolean active;
	@SuppressWarnings("unused")
	private final Boolean penalty;

	public SavingsGroupChargeData(final Long id,
			final EnumOptionData chargeAppliesToEnum,
			final EnumOptionData chargeTimeEnum,
			final EnumOptionData chargeCalculationEnum,
			final BigDecimal amount, final Boolean active, final Boolean penalty) {
		this.id = id;
		this.chargeAppliesToEnum = chargeAppliesToEnum;
		this.chargeTimeEnum = chargeTimeEnum;
		this.chargeCalculationEnum = chargeCalculationEnum;
		this.amount = amount;
		this.active = active;
		this.penalty = penalty;
	}
}
