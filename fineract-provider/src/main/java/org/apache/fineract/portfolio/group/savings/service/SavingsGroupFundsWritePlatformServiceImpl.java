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
package org.apache.fineract.portfolio.group.savings.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupTypeEnum;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.apache.fineract.portfolio.group.savings.data.SGFundsDataValidator;
import org.apache.fineract.portfolio.group.savings.domain.SGCycleRepository;
import org.apache.fineract.portfolio.group.savings.domain.SGFundsRepository;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupCycle;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupCycleStatusEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupFund;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupFundStatusEnum;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidStatusToProcessRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsGroupFundsWritePlatformServiceImpl implements
		SavingsGroupFundsWritePlatformService {

	private final GroupRepositoryWrapper groupRepoWrapper;
	private final SGCycleRepository cycleRepo;
	private final SGFundsRepository fundsRepo;
	private final SGFundsDataValidator fundsDataValidator;
	private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;

	@Autowired
	public SavingsGroupFundsWritePlatformServiceImpl(
			final SGFundsRepository fundsRepo,
			final SGFundsDataValidator fundsDataValidator,
			final GroupRepositoryWrapper groupRepoWrapper,
			final SGCycleRepository cycleRepo,
			final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository) {
		this.fundsRepo = fundsRepo;
		this.fundsDataValidator = fundsDataValidator;
		this.groupRepoWrapper = groupRepoWrapper;
		this.cycleRepo = cycleRepo;
		this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
	}

	@Override
	public CommandProcessingResult createFund(final JsonCommand command) {

		final Long groupId = command.entityId();
		Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycle latestCycle = this.cycleRepo
				.getLatestCycle(groupId);
		if (null == latestCycle
				|| !SavingsGroupCycleStatusEnum.fromInt(
						latestCycle.getStatusEnum()).hasStateOf(
						SavingsGroupCycleStatusEnum.INITIATED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		this.fundsDataValidator.validateForCreateFund(command);

		final Long transactionProcessingStrategyId = command
				.longValueOfParameterNamed(SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName);
		final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyById(transactionProcessingStrategyId);

		final SavingsGroupFund newFund = SavingsGroupFund.createWith(command,
				group, latestCycle, loanTransactionProcessingStrategy);
		final LoanProduct loanProduct = createLoanProduct(newFund);
		newFund.updateLoanProduct(loanProduct);
		this.fundsRepo.save(newFund);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.withSubEntityId(newFund.getId()) //
				.build();
	}

	@Override
	public CommandProcessingResult updateFund(final JsonCommand command) {
		final Long groupId = command.getGroupId();
		Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final Long fundId = command.entityId();
		final SavingsGroupFund fund = this.fundsRepo.findOne(fundId);
		if (fund == null) {
			throw new SGPlatformResourceNotFoundException("fund.not.found",
					"Fund with id " + fundId + " not found");
		}
		
		if(!fund.getGroup().getId().equals(groupId)){
			throw new SGInvalidRequestException("fund.does.not.belong.to.group",
					"Requested Fund Id is not associated with given Group Id");
		}

		final SavingsGroupCycle latestCycle = fund.getCycle();
		final Integer latestCycleStatus = latestCycle.getStatusEnum();
		if (SavingsGroupCycleStatusEnum.fromInt(latestCycleStatus).hasStateOf(
				SavingsGroupCycleStatusEnum.CLOSED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		if (SavingsGroupFundStatusEnum.fromInt(fund.getStatus()).hasStateOf(
				SavingsGroupFundStatusEnum.INACTIVE)) {
			throw new SGInvalidStatusToProcessRequestException(
					"fund.invalid.request.based.on.status",
					"Request is not valid because of current savings group fund status");
		}

		if (SavingsGroupCycleStatusEnum.fromInt(latestCycleStatus).hasStateOf(
				SavingsGroupCycleStatusEnum.INITIATED)) {
			this.fundsDataValidator
					.validateForUpdateFundInitiateStatus(command);
		} else {
			this.fundsDataValidator.validateForUpdateFundActiveStatus(command);
		}

		Map<String, Object> changes = null;
		if (command
				.hasParameter(SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName)) {
			final Long transactionProcessingStrategyId = command
					.longValueOfParameterNamed(SavingsGroupAPIConstants.transactionProcessingStrategyIdParamName);
			final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyById(transactionProcessingStrategyId);
			changes = fund.update(command, loanTransactionProcessingStrategy);
		} else {
			changes = fund.update(command);
		}

		this.fundsRepo.save(fund);
		updateProductAndCharges(fund, changes);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(fund.getGroup().getId()) //
				.withSubEntityId(fund.getId()) //
				.with(encloseWithFundResourceName(changes)) //
				.build();
	}

	@Override
	public CommandProcessingResult deleteFund(final JsonCommand command) {
		final Long groupId = command.getGroupId();
		Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final Long fundId = command.entityId();
		final SavingsGroupFund fund = this.fundsRepo.findOne(fundId);
		if (fund == null) {
			throw new SGPlatformResourceNotFoundException("fund.not.found",
					"Fund with id " + fundId + " not found");
		}

		if(!fund.getGroup().getId().equals(groupId)){
			throw new SGInvalidRequestException("fund.does.not.belong.to.group",
					"Requested Fund Id is not associated with given Group Id");
		}

		final SavingsGroupCycle latestCycle = fund.getCycle();
		final Integer latestCycleStatus = latestCycle.getStatusEnum();
		if (!SavingsGroupCycleStatusEnum.fromInt(latestCycleStatus).hasStateOf(
				SavingsGroupCycleStatusEnum.INITIATED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		if (!SavingsGroupFundStatusEnum.fromInt(fund.getStatus()).hasStateOf(
				SavingsGroupFundStatusEnum.ACTIVE)) {
			throw new SGInvalidStatusToProcessRequestException(
					"fund.invalid.request.based.on.status",
					"Request is not valid because of current savings group fund status");
		}

		Map<String, Object> changes = fund.deleteFund();
		this.fundsRepo.save(fund);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(fund.getGroup().getId()) //
				.withSubEntityId(fund.getId()) //
				.with(encloseWithFundResourceName(changes)) //
				.build();
	}

	private LoanTransactionProcessingStrategy findStrategyById(
			final Long transactionProcessingStrategyId) {
		LoanTransactionProcessingStrategy strategy = null;
		strategy = this.loanTransactionProcessingStrategyRepository
				.findOne(transactionProcessingStrategyId);
		if (strategy == null) {
			throw new LoanTransactionProcessingStrategyNotFoundException(
					transactionProcessingStrategyId);
		}
		return strategy;
	}

	private LoanProduct createLoanProduct(final SavingsGroupFund newFund) {
		// TODO Auto-generated method stub
		return null;
	}

	private void updateProductAndCharges(SavingsGroupFund fund,
			Map<String, Object> changes) {
		// TODO Auto-generated method stub

	}

	private Map<String, Object> encloseWithFundResourceName(
			Map<String, Object> changes) {
		Map<String, Object> fund = new HashMap<>();
		fund.put(SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME, changes);
		return fund;
	}

	@Override
	public void copyFundsFromCycle(final Long sourceCycleId,
			SavingsGroupCycle destCycle) {
		Collection<SavingsGroupFund> sourceFunds = this.fundsRepo
				.findAllActiveFundsWithCycle(sourceCycleId);
		if (sourceFunds != null && sourceFunds.size() > 0) {
			Collection<SavingsGroupFund> newCycleFunds = new ArrayList<>();
			for (SavingsGroupFund sourceFund : sourceFunds) {
				newCycleFunds.add(sourceFund.copy(destCycle));
			}
			this.fundsRepo.save(newCycleFunds);
		}
	}
}
