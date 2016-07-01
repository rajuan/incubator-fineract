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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.domain.GroupTypeEnum;
import org.apache.fineract.portfolio.group.savings.data.SavingsGroupCycleData;
import org.apache.fineract.portfolio.group.savings.domain.SavingsGroupDepositsPaymentStrategyEnum;
import org.apache.fineract.portfolio.group.savings.exception.SGInvalidRequestException;
import org.apache.fineract.portfolio.group.savings.exception.SGPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.group.savings.service.SavingsGroupCycleReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/groups/{groupId}/cycle")
@Component
@Scope("singleton")
public class SavingsGroupCycleApiResource {

	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<SavingsGroupCycleData> groupSerializer;
	private final ToApiJsonSerializer<Object> objectSerializer;
	private final SavingsGroupCycleReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final CurrencyReadPlatformService currencyReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final GroupReadPlatformService groupReadPlatformService;

	@Autowired
	public SavingsGroupCycleApiResource(
			final PlatformSecurityContext context,
			final ToApiJsonSerializer<SavingsGroupCycleData> groupSerializer,
			final ToApiJsonSerializer<Object> objectSerializer,
			final SavingsGroupCycleReadPlatformService readPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final CurrencyReadPlatformService currencyReadPlatformService,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final GroupReadPlatformService groupReadPlatformService) {
		this.context = context;
		this.groupSerializer = groupSerializer;
		this.objectSerializer = objectSerializer;
		this.readPlatformService = readPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.currencyReadPlatformService = currencyReadPlatformService;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.groupReadPlatformService = groupReadPlatformService;
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo,
			@PathParam("groupId") final Long groupId) {
		this.context.authenticatedUser().validateHasReadPermission(
				SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		GroupGeneralData group = this.groupReadPlatformService
				.retrieveOne(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType().getId().intValue())
				.hasStateOf(GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		SavingsGroupCycleData latestCycleData = this.readPlatformService
				.getLatestCycleData(groupId);
		final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService
				.retrieveAllowedCurrencies();
		final Collection<EnumOptionData> depositsPaymentStrategyOptions = SavingsGroupDepositsPaymentStrategyEnum
				.getSavingsGroupDepositsPaymentStrategyOptions();
		SavingsGroupCycleData retData = null;
		if (null != latestCycleData) {
			retData = SavingsGroupCycleData.getTemplate(latestCycleData,
					currencyOptions, depositsPaymentStrategyOptions);
		} else {
			retData = SavingsGroupCycleData.getTemplate(currencyOptions,
					depositsPaymentStrategyOptions);
		}

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.groupSerializer.serialize(settings, retData,
				SavingsGroupAPIConstants.CYCLE_ALLOWED_RESPONSE_PARAMS);

	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLatestCycle(@Context final UriInfo uriInfo,
			@PathParam("groupId") final Long groupId) {
		this.context.authenticatedUser().validateHasReadPermission(
				SavingsGroupAPIConstants.CYCLE_RESOURCE_NAME);

		GroupGeneralData group = this.groupReadPlatformService
				.retrieveOne(groupId);
		if (!GroupTypeEnum.fromInt(group.getGroupType().getId().intValue())
				.hasStateOf(GroupTypeEnum.SAVINGS)) {
			throw new SGInvalidRequestException("not.savings.group",
					"Requested service is not valid for groups that are not of groupType.SAVINGS");
		}

		SavingsGroupCycleData latestCycleData = this.readPlatformService
				.getLatestCycleData(groupId);
		if(null == latestCycleData){
			throw new SGPlatformResourceNotFoundException("cycle.none.defined.for.group",
					"No Cycle is ever defined for this group");
		}
		
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.groupSerializer.serialize(settings, latestCycleData,
				SavingsGroupAPIConstants.CYCLE_ALLOWED_RESPONSE_PARAMS);

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String create(@PathParam("groupId") final Long groupId,
			final String apiRequestBodyAsJson,
			@QueryParam("command") final String commandParam) {

		final CommandWrapperBuilder builder = new CommandWrapperBuilder()
				.withJson(apiRequestBodyAsJson);
		CommandProcessingResult result = null;

		if (StringUtils.isBlank(commandParam)) {
			final CommandWrapper commandRequest = builder
					.initiateCycle(groupId).build();
			result = this.commandsSourceWritePlatformService
					.logCommandSource(commandRequest);
			return this.objectSerializer.serialize(result);
		} else if (is(commandParam, "activate")) {
			final CommandWrapper commandRequest = builder
					.activateCycle(groupId).build();
			result = this.commandsSourceWritePlatformService
					.logCommandSource(commandRequest);
			return this.objectSerializer.serialize(result);
		} else if (is(commandParam, "shareout")) {
			final CommandWrapper commandRequest = builder
					.shareoutCycle(groupId).build();
			result = this.commandsSourceWritePlatformService
					.logCommandSource(commandRequest);
			return this.objectSerializer.serialize(result);
		} else if (is(commandParam, "shareoutclose")) {
			final CommandWrapper commandRequest = builder.shareoutCloseCycle(
					groupId).build();
			result = this.commandsSourceWritePlatformService
					.logCommandSource(commandRequest);
			return this.objectSerializer.serialize(result);
		} else {
			throw new UnrecognizedQueryParamException("command", commandParam,
					new Object[] { "activate", "shareout", "shareoutclose" });
		}
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String update(@PathParam("groupId") final Long groupId,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.updateCycle(groupId) //
				.withJson(apiRequestBodyAsJson) //
				.build(); //
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.objectSerializer.serialize(result);
	}

	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam)
				&& commandParam.trim().equalsIgnoreCase(commandValue);
	}

}
