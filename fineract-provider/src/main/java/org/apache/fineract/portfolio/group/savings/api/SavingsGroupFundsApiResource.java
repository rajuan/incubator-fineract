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
package org.apache.fineract.portfolio.group.savings.api;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.domain.GroupTypeEnum;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupFundData;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeAppliesToEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeCalculationEnum;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupChargeTimeEnum;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.group.savings.service.SavingsGroupFundsReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/groups/{groupId}/funds")
@Component
@Scope("singleton")
public class SavingsGroupFundsApiResource {

	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<SavingsGroupFundData> fundSerializer;
	private final ToApiJsonSerializer<Object> objectSerializer;
	private final SavingsGroupFundsReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final GroupReadPlatformService groupReadPlatformService;
	private final LoanDropdownReadPlatformService dropdownReadPlatformService;

	@Autowired
	public SavingsGroupFundsApiResource(
			final PlatformSecurityContext context,
			final ToApiJsonSerializer<SavingsGroupFundData> fundSerializer,
			final ToApiJsonSerializer<Object> objectSerializer,
			final SavingsGroupFundsReadPlatformService readPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final GroupReadPlatformService groupReadPlatformService,
			final LoanDropdownReadPlatformService dropdownReadPlatformService) {
		this.context = context;
		this.fundSerializer = fundSerializer;
		this.objectSerializer = objectSerializer;
		this.readPlatformService = readPlatformService;
		this.groupReadPlatformService = groupReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.dropdownReadPlatformService = dropdownReadPlatformService;
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo,
			@PathParam("groupId") final Long groupId) {
		this.context.authenticatedUser().validateHasReadPermission(
				SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		GroupGeneralData group = this.groupReadPlatformService
				.retrieveOne(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType().getId().intValue())
				.hasStateOf(GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final List<EnumOptionData> interestMethodOptions = this.dropdownReadPlatformService
				.retrieveLoanInterestTypeOptions();
		final List<EnumOptionData> interestCalculatedInPeriodOptions = this.dropdownReadPlatformService
				.retrieveLoanInterestRateCalculatedInPeriodOptions();
		final List<EnumOptionData> repaymentPeriodFrequencyOptions = this.dropdownReadPlatformService
				.retrieveRepaymentFrequencyTypeOptions();
		final List<EnumOptionData> amortizationMethodOptions = this.dropdownReadPlatformService
				.retrieveLoanAmortizationTypeOptions();
		final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService
				.retreiveTransactionProcessingStrategies();

		final List<EnumOptionData> chargeAppliesToOptions = SavingsGroupChargeAppliesToEnum
				.getChargeAppliesToOptions();
		final List<EnumOptionData> loanChargeTimeOptions = SavingsGroupChargeTimeEnum
				.getLoanChargeTimeOptions();
		final List<EnumOptionData> loanChargeCalculationOptions = SavingsGroupChargeCalculationEnum
				.getLoanChargeCalculationOptions();
		final List<EnumOptionData> groupChargeTimeOptions = SavingsGroupChargeTimeEnum
				.getGroupChargeTimeOptions();
		final List<EnumOptionData> groupChargeCalculationOptions = SavingsGroupChargeCalculationEnum
				.getGroupChargeCalculationOptions();

		final SavingsGroupFundData retData = SavingsGroupFundData.template(
				interestMethodOptions, interestCalculatedInPeriodOptions,
				repaymentPeriodFrequencyOptions, amortizationMethodOptions,
				transactionProcessingStrategyOptions, chargeAppliesToOptions,
				loanChargeTimeOptions, loanChargeCalculationOptions,
				groupChargeTimeOptions, groupChargeCalculationOptions);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.fundSerializer.serialize(settings, retData,
				SavingsGroupAPIConstants.FUND_ALLOWED_RESPONSE_PARAMS);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createFund(@PathParam("groupId") final Long groupId,
			final String apiRequestBodyAsJson) {
		final CommandWrapperBuilder builder = new CommandWrapperBuilder()
				.withJson(apiRequestBodyAsJson);
		final CommandWrapper commandRequest = builder.createSGFund(groupId)
				.build();
		CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.objectSerializer.serialize(result);
	}

	@PUT
	@Path("{fundId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateFund(@PathParam("groupId") final Long groupId,
			@PathParam("fundId") final Long fundId,
			final String apiRequestBodyAsJson) {
		final CommandWrapperBuilder builder = new CommandWrapperBuilder()
				.withJson(apiRequestBodyAsJson);
		final CommandWrapper commandRequest = builder.updateSGFund(groupId,
				fundId).build();
		CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.objectSerializer.serialize(result);
	}

	@DELETE
	@Path("{fundId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteFund(@PathParam("groupId") final Long groupId,
			@PathParam("fundId") final Long fundId,
			final String apiRequestBodyAsJson) {
		final CommandWrapperBuilder builder = new CommandWrapperBuilder()
				.withJson(apiRequestBodyAsJson);
		final CommandWrapper commandRequest = builder.deleteSGFund(groupId,
				fundId).build();
		CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.objectSerializer.serialize(result);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllFundsOfCurrentCycle(
			@Context final UriInfo uriInfo,
			@PathParam("groupId") final Long groupId) {
		this.context.authenticatedUser().validateHasReadPermission(
				SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		GroupGeneralData group = this.groupReadPlatformService
				.retrieveOne(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType().getId().intValue())
				.hasStateOf(GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final Collection<SavingsGroupFundData> retData = this.readPlatformService
				.getLatestCycleFundsData(groupId);
		if(null == retData || retData.size() == 0){
			throw new SGPlatformResourceNotFoundException("fund.none.defined.for.latest.cycle", 
					"No Funds defined for the current cycle");
		}

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.fundSerializer.serialize(settings, retData,
				SavingsGroupAPIConstants.FUND_ALLOWED_RESPONSE_PARAMS);
	}

	@GET
	@Path("{fundId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveFundOfCurrentCycle(@Context final UriInfo uriInfo,
			@PathParam("groupId") final Long groupId,
			@PathParam("fundId") final Long fundId) {
		this.context.authenticatedUser().validateHasReadPermission(
				SavingsGroupAPIConstants.FUNDS_RESOURCE_NAME);

		GroupGeneralData group = this.groupReadPlatformService
				.retrieveOne(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType().getId().intValue())
				.hasStateOf(GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		final SavingsGroupFundData retData = this.readPlatformService
				.getSavingsGroupFundData(fundId, groupId);

		if(null == retData){
			throw new SGPlatformResourceNotFoundException("fund.not.found", 
					"Fund not found");
		}
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.fundSerializer.serialize(settings, retData,
				SavingsGroupAPIConstants.FUND_ALLOWED_RESPONSE_PARAMS);
	}
}
