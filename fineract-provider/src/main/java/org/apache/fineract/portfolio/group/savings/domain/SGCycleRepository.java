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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SGCycleRepository extends
		JpaRepository<SavingsGroupCycle, Long>,
		JpaSpecificationExecutor<SavingsGroupCycle> {

	public static final String FIND_LATEST_CYCLE_BY_GROUP = "from SavingsGroupCycle lsgc "
			+ " where lsgc.group.id = :groupId "
			+ " and lsgc.cycleNumber = (select max(c1.cycleNumber) from SavingsGroupCycle c1 where c1.group.id = :groupId)";

	@Query(FIND_LATEST_CYCLE_BY_GROUP)
	SavingsGroupCycle getLatestCycle(@Param("groupId") Long groupId);
}
