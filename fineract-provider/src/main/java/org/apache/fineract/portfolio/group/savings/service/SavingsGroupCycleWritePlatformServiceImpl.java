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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupTypeEnum;
import org.apache.fineract.portfolio.group.savings.api.SavingsGroupAPIConstants;
import org.apache.fineract.portfolio.group.savings.data.SGCycleDataValidator;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupCycleData;
import org.apache.fineract.portfolio.group.savings.domain.SGCycleRepository;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupCycle;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupCycleStatusEnum;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidStatusToProcessRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsGroupCycleWritePlatformServiceImpl implements
		SavingsGroupCycleWritePlatformService {

	private final GroupRepositoryWrapper groupRepoWrapper;
	private final SGCycleRepository cycleRepo;
	private final SavingsGroupCycleReadPlatformService cycleReadPlatformService;
	private final CalendarInstanceRepository calendarInstanceRepo;
	private final SGCycleDataValidator cycleDataValidator;
	private final SavingsGroupFundsWritePlatformService fundsWritePlatformService;

	@Autowired
	public SavingsGroupCycleWritePlatformServiceImpl(
			final GroupRepositoryWrapper groupRepoWrapper,
			final SGCycleRepository cycleRepo,
			final SavingsGroupCycleReadPlatformService cycleReadPlatformService,
			final CalendarInstanceRepository calendarInstanceRepo,
			final SGCycleDataValidator cycleDataValidator,
			final SavingsGroupFundsWritePlatformService fundsWritePlatformService) {
		this.groupRepoWrapper = groupRepoWrapper;
		this.cycleRepo = cycleRepo;
		this.cycleReadPlatformService = cycleReadPlatformService;
		this.calendarInstanceRepo = calendarInstanceRepo;
		this.cycleDataValidator = cycleDataValidator;
		this.fundsWritePlatformService = fundsWritePlatformService;
	}

	@Override
	@Transactional
	public CommandProcessingResult createCycle(JsonCommand command) {
		final Long groupId = command.entityId();
		final Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycleData latestCycleData = this.cycleReadPlatformService
				.getLatestCycleData(groupId);
		if (null != latestCycleData
				&& !SavingsGroupCycleStatusEnum.fromInt(
						latestCycleData.getStatus().getId().intValue())
						.hasStateOf(SavingsGroupCycleStatusEnum.CLOSED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}
		final Long lastCycleNum = null == latestCycleData ? 0 : latestCycleData
				.getCycleNumber();

		this.cycleDataValidator.validateForCreateCycle(command);
		final String code = command
				.stringValueOfParameterNamed(SavingsGroupAPIConstants.currencyCodeParamName);
		final Integer digitsAfterDecimal = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.currencyDigitsParamName);
		final Integer inMultiplesOf = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.currencyMultiplesOfParamName);
		final MonetaryCurrency currency = new MonetaryCurrency(code,
				digitsAfterDecimal, inMultiplesOf);
		final LocalDate expectedStartDate = command
				.localDateValueOfParameterNamed(SavingsGroupAPIConstants.startDateParamName);
		final LocalDate actualStartDate = null;
		final LocalDate expectedEndDate = command
				.localDateValueOfParameterNamed(SavingsGroupAPIConstants.endDateParamName);
		final LocalDate actualEndDate = null;
		final Boolean isShareBased = command
				.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isShareBasedParamName);
		BigDecimal unitPriceOfShare = command
				.bigDecimalValueOfParameterNamed(SavingsGroupAPIConstants.unitPriceOfShareParamName);
		if(!isShareBased){
			unitPriceOfShare = BigDecimal.ONE;
		}
		final Boolean isClientAdditionsAllowedInActiveCycle = command
				.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isClientAdditionsAllowedInActiveCycleParamName);
		final Boolean isClientExitAllowedInActiveCycle = command
				.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.isClientExitAllowedInActiveCycleParamName);
		final Boolean doesIndividualClientExitForfeitGains = command
				.booleanObjectValueOfParameterNamed(SavingsGroupAPIConstants.doesIndividualClientExitForfeitGainsParamName);
		final Integer depositsPaymentStrategyEnum = command
				.integerValueOfParameterNamed(SavingsGroupAPIConstants.depositsPaymentStrategyIdParamName);

		if(expectedStartDate.isBefore(group.getActivationLocalDate())){
			throw new SGInvalidRequestException(
					"cycle.startdate.should.be.after.group.activation.date",
					"Cycle start date should be after group activation date.");
		}
		final CalendarInstance groupCalInst = this.calendarInstanceRepo
				.findCalendarInstaneByEntityId(groupId,
						CalendarEntityType.GROUPS.getValue());
		if (null == groupCalInst) {
			throw new SGInvalidRequestException(
					"meeting.not.setup",
					"Request is not valid because meeting calendar is not attached to group");
		}
		final Calendar groupCal = groupCalInst.getCalendar();
		if (!CalendarUtils.isValidRedurringDate(groupCal.getRecurrence(),
				groupCal.getStartDateLocalDate(), expectedStartDate)) {
			throw new SGInvalidRequestException(
					"cycle.startdate.is.not.valid.meeting.date",
					"Start Date param is not a valid meeting recurrence date");
		}
		if (!CalendarUtils.isValidRedurringDate(groupCal.getRecurrence(),
				groupCal.getStartDateLocalDate(), expectedEndDate)) {
			throw new SGInvalidRequestException(
					"cycle.enddate.is.not.valid.meeting.date",
					"End Date param is not a valid meeting recurrence date");
		}
		if (expectedEndDate.isBefore(expectedStartDate)) {
			throw new SGInvalidRequestException(
					"enddate.should.be.after.startdate",
					"Cycle End Date should be after Cycle Start Date");
		}
		Integer expectedNumOfMeetings = CalendarUtils.getRecurringDates(
					groupCal.getRecurrence(), groupCal.getStartDateLocalDate(),
					expectedStartDate, expectedEndDate, -1, false, 0).size() + 1; //since both start and end dates are forced to be on meeting dates, increase the count by 1
		final Integer numOfMeetingsCompleted = 0;
		final Integer numOfMeetingsPending = 0;

		ShareProduct shareProduct = null;
		if (null == latestCycleData) {
			shareProduct = createShareProductWith(group, currency,
					unitPriceOfShare);
		} else {
			shareProduct = updateShareProductWith(
					latestCycleData.getShareProductId(), currency,
					unitPriceOfShare);
		}

		final SavingsGroupCycle newCycle = SavingsGroupCycle.newCycle(group,
				lastCycleNum + 1, currency, expectedStartDate, actualStartDate,
				expectedEndDate, actualEndDate, expectedNumOfMeetings,
				numOfMeetingsCompleted, numOfMeetingsPending, isShareBased,
				unitPriceOfShare, shareProduct,
				isClientAdditionsAllowedInActiveCycle,
				isClientExitAllowedInActiveCycle,
				doesIndividualClientExitForfeitGains,
				depositsPaymentStrategyEnum);
		this.cycleRepo.save(newCycle);
		if (latestCycleData != null
				&& command
						.hasParameter(SavingsGroupAPIConstants.copyFundsFromPreviousCycle)
				&& command
						.booleanPrimitiveValueOfParameterNamed(SavingsGroupAPIConstants.copyFundsFromPreviousCycle)) {
			this.fundsWritePlatformService.copyFundsFromCycle(
					latestCycleData.getId(), newCycle);
		}
		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.withSubEntityId(newCycle.getId()) //
				.build();
	}

	private ShareProduct updateShareProductWith(final Long shareProductId,
			final MonetaryCurrency currency, final BigDecimal unitPriceOfShare) {
		// TODO Auto-generated method stub
		return null;
	}

	private ShareProduct updateShareProductWith(
			final ShareProduct shareProduct, final MonetaryCurrency currency,
			final BigDecimal unitPriceOfShare) {
		// TODO Auto-generated method stub
		return null;
	}

	private ShareProduct createShareProductWith(final Group group,
			final MonetaryCurrency currency, final BigDecimal unitPriceOfShare) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public CommandProcessingResult activateCycle(JsonCommand command) {
		final Long groupId = command.entityId();
		final Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycle latestCycle = this.cycleRepo
				.getLatestCycle(groupId);
		if (null == latestCycle){
			throw new SGPlatformResourceNotFoundException("cycle.not.found", 
					"Cycle not found");
		}
		
		if(!SavingsGroupCycleStatusEnum.fromInt(
						latestCycle.getStatusEnum()).hasStateOf(
						SavingsGroupCycleStatusEnum.INITIATED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		this.cycleDataValidator.validateForActivateCycle(command);

		final LocalDate expectedStartDate = command
				.localDateValueOfParameterNamed(SavingsGroupAPIConstants.startDateParamName);
		if(expectedStartDate.isBefore(group.getActivationLocalDate())){
			throw new SGInvalidRequestException(
					"cycle.startdate.should.be.after.group.activation.date",
					"Cycle start date should be after group activation date.");
		}
		final CalendarInstance groupCalInst = this.calendarInstanceRepo
				.findCalendarInstaneByEntityId(groupId,
						CalendarEntityType.GROUPS.getValue());
		if (null == groupCalInst) {
			throw new SGInvalidStatusToProcessRequestException(
					"meeting.not.setup",
					"Request is not valid because meeting calendar is not attached to group");
		}
		final Calendar groupCal = groupCalInst.getCalendar();

		final LocalDate startDate = command
				.localDateValueOfParameterNamed(SavingsGroupAPIConstants.startDateParamName);
		if (!CalendarUtils.isValidRedurringDate(groupCal.getRecurrence(),
				groupCal.getStartDateLocalDate(), startDate)) {
			throw new SGInvalidRequestException(
					"cycle.startdate.is.not.valid.meeting.date",
					"Start Date param is not a valid meeting recurrence date");
		}

		if (!latestCycle.getExpectedEndDate().isAfter(startDate)) {
			throw new SGInvalidRequestException(
					"enddate.should.be.after.startdate",
					"Cycle End Date should be after Cycle Start Date");
		}

		final Map<String, Object> changes = latestCycle
				.activateCycle(startDate);

		this.cycleRepo.save(latestCycle);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.with(encloseWithCycleResourceName(changes)) //
				.build();
	}

	@Override
	@Transactional
	public CommandProcessingResult updateCycle(JsonCommand command) {
		final Long groupId = command.entityId();
		final Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycle latestCycle = this.cycleRepo
				.getLatestCycle(groupId);
		if (null == latestCycle){
			throw new SGPlatformResourceNotFoundException("cycle.not.found", 
					"Cycle not found");
		}
		if (!SavingsGroupCycleStatusEnum.fromInt(
						latestCycle.getStatusEnum()).hasStateOf(
						SavingsGroupCycleStatusEnum.INITIATED)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		this.cycleDataValidator.validateForUpdateCycle(command);

		boolean startDateToBeUpdated = command
				.parameterExists(SavingsGroupAPIConstants.startDateParamName);
		boolean endDateToBeUpdated = command
				.parameterExists(SavingsGroupAPIConstants.endDateParamName);
		Integer expectedNumOfMeetings = null;
		if (startDateToBeUpdated || endDateToBeUpdated) {
			LocalDate startDate = latestCycle.getExpectedStartDate();
			LocalDate endDate = latestCycle.getExpectedEndDate();

			if(startDate.isBefore(group.getActivationLocalDate())){
				throw new SGInvalidRequestException(
						"cycle.startdate.should.be.after.group.activation.date",
						"Cycle start date should be after group activation date.");
			}
			final CalendarInstance groupCalInst = this.calendarInstanceRepo
					.findCalendarInstaneByEntityId(groupId,
							CalendarEntityType.GROUPS.getValue());
			if (null == groupCalInst) {
				throw new SGInvalidStatusToProcessRequestException(
						"meeting.not.setup",
						"Request is not valid because meeting calendar is not attached to group");
			}
			final Calendar groupCal = groupCalInst.getCalendar();
			if (startDateToBeUpdated) {
				startDate = command
						.localDateValueOfParameterNamed(SavingsGroupAPIConstants.startDateParamName);
				if (!CalendarUtils.isValidRedurringDate(
						groupCal.getRecurrence(),
						groupCal.getStartDateLocalDate(), startDate)) {
					throw new SGInvalidRequestException(
							"cycle.startdate.is.not.valid.meeting.date",
							"Start Date param is not a valid meeting recurrence date");
				}
			}
			if (endDateToBeUpdated) {
				endDate = command
						.localDateValueOfParameterNamed(SavingsGroupAPIConstants.endDateParamName);
				if (!CalendarUtils.isValidRedurringDate(
						groupCal.getRecurrence(),
						groupCal.getStartDateLocalDate(), endDate)) {
					throw new SGInvalidRequestException(
							"cycle.enddate.is.not.valid.meeting.date",
							"End Date param is not a valid meeting recurrence date");
				}
			}
			if (!endDate.isAfter(startDate)) {
				throw new SGInvalidRequestException(
						"enddate.should.be.after.startdate",
						"Cycle End Date should be after Cycle Start Date");
			}

			expectedNumOfMeetings = CalendarUtils.getRecurringDates(
					groupCal.getRecurrence(), groupCal.getStartDateLocalDate(),
					startDate, endDate, -1, false, 0).size() + 1;
		}

		Map<String, Object> changes = latestCycle.updateCycle(command,
				expectedNumOfMeetings);
		if (changes.containsKey(SavingsGroupAPIConstants.currencyParamName)
				|| changes
						.containsKey(SavingsGroupAPIConstants.unitPriceOfShareParamName)) {
			final ShareProduct shareProduct = updateShareProductWith(
					latestCycle.getShareProduct(), latestCycle.getCurrency(),
					latestCycle.getUnitPriceOfShare());
			latestCycle.setShareProduct(shareProduct);
		}
		this.cycleRepo.save(latestCycle);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.withSubEntityId(latestCycle.getId()) //
				.with(encloseWithCycleResourceName(changes)) //
				.build();
	}

	@Override
	@Transactional
	public CommandProcessingResult shareOutCycle(JsonCommand command) {
		// TODO Auto-generated method stub
		final Long groupId = command.entityId();
		final Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycle latestCycle = this.cycleRepo
				.getLatestCycle(groupId);
		if (null == latestCycle){
			throw new SGPlatformResourceNotFoundException("cycle.not.found", 
					"Cycle not found");
		}
		if(!SavingsGroupCycleStatusEnum.fromInt(
						latestCycle.getStatusEnum()).hasStateOf(
						SavingsGroupCycleStatusEnum.ACTIVE)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}


		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.build();	}

	@Override
	@Transactional
	public CommandProcessingResult shareOutCloseCycle(JsonCommand command) {
		// TODO Auto-generated method stub
		final Long groupId = command.entityId();
		final Group group = this.groupRepoWrapper
				.findOneWithNotFoundDetection(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType()).hasStateOf(
				GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupCycle latestCycle = this.cycleRepo
				.getLatestCycle(groupId);
		if (null == latestCycle){
			throw new SGPlatformResourceNotFoundException("cycle.not.found", 
					"Cycle not found");
		}
		if(!SavingsGroupCycleStatusEnum.fromInt(
						latestCycle.getStatusEnum()).hasStateOf(
						SavingsGroupCycleStatusEnum.ACTIVE)) {
			throw new SGInvalidStatusToProcessRequestException(
					"cycle.invalid.request.based.on.status",
					"Request is not valid because of current savings group cycle status");
		}

		this.cycleDataValidator.validateForShareOutCloseCycle(command);

		final LocalDate endDate = command
				.localDateValueOfParameterNamed(SavingsGroupAPIConstants.endDateParamName);
		if(endDate.isBefore(latestCycle.getStartDate())){
			throw new SGInvalidRequestException(
					"cycle.enddate.should.be.after.cycle.startdate",
					"Cycle end date should be after cycle start date.");
		}
		final Map<String, Object> changes = latestCycle.closeCycle(endDate);

		this.cycleRepo.save(latestCycle);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(groupId) //
				.with(encloseWithCycleResourceName(changes)) //
				.build();
	}

	private Map<String, Object> encloseWithCycleResourceName(
			Map<String, Object> changes) {
		Map<String, Object> latestCycle = new HashMap<>();
		latestCycle.put(SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME, changes);
		return latestCycle;
	}

}
