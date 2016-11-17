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
package org.apache.fineract.infrastructure.dataqueries.service;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecks;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.infrastructure.dataqueries.exception.*;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@Service
public class EntityDatatableChecksWritePlatformServiceImpl implements EntityDatatableChecksWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(EntityDatatableChecksWritePlatformServiceImpl.class);

	private final PlatformSecurityContext context;
	private final EntityDatatableChecksDataValidator fromApiJsonDeserializer;
	private final EntityDatatableChecksRepository entityDatatableChecksRepository;
	private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
	private final LoanProductReadPlatformService loanProductReadPlatformService;
	private final SavingsProductReadPlatformService savingsProductReadPlatformService;

	@Autowired
	public EntityDatatableChecksWritePlatformServiceImpl(final PlatformSecurityContext context,
			final EntityDatatableChecksDataValidator fromApiJsonDeserializer,
			final EntityDatatableChecksRepository entityDatatableChecksRepository,
			final ReadWriteNonCoreDataService readWriteNonCoreDataService,
			final LoanProductReadPlatformService loanProductReadPlatformService,
			final SavingsProductReadPlatformService savingsProductReadPlatformService) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.entityDatatableChecksRepository = entityDatatableChecksRepository;
		this.readWriteNonCoreDataService = readWriteNonCoreDataService;
		this.loanProductReadPlatformService = loanProductReadPlatformService;
		this.savingsProductReadPlatformService = savingsProductReadPlatformService;

	}

	@Transactional
	@Override
	public CommandProcessingResult createCheck(final JsonCommand command) {

		try {
			this.context.authenticatedUser();

			this.fromApiJsonDeserializer.validateForCreate(command.json());

			// check if the datatable is linked to the entity

			String datatableName = command.stringValueOfParameterNamed("datatableName");
			DatatableData datatableData = this.readWriteNonCoreDataService.retrieveDatatable(datatableName);

			if (datatableData == null) {
				throw new DatatableNotFoundException(datatableName);
			}

			final String entity = command.stringValueOfParameterNamed("entity");
			final String foreignKeyColumnName = EntityTables.getForeignKeyColumnNameOnDatatable(entity);
			final boolean columnExist = datatableData.hasColumn(foreignKeyColumnName);

			logger.info(datatableData.getRegisteredTableName() + "has column " + foreignKeyColumnName + " ? "
					+ columnExist);

			if (!columnExist) {
				throw new EntityDatatableCheckNotSupportedException(datatableData.getRegisteredTableName(), entity);
			}

			final Long productId = command.longValueOfParameterNamed("productId");
			final Long status = command.longValueOfParameterNamed("status");


			List<EntityDatatableChecks> entityDatatableCheck = null;
			if (productId == null) {
				entityDatatableCheck = this.entityDatatableChecksRepository
						.findByEntityStatusAndDatatableIdAndNoProduct(entity, status, datatableName);
				if (!entityDatatableCheck.isEmpty()) {
					throw new EntityDatatableCheckAlreadyExistsException(entity, status, datatableName);
				}
			} else {
				if (entity.equals("m_loan")) {
					// if invalid loan product id, throws exception
					this.loanProductReadPlatformService.retrieveLoanProduct(productId);
				} else if (entity.equals("m_savings_account")) {
					// if invalid savings product id, throws exception
					this.savingsProductReadPlatformService.retrieveOne(productId);
				} else {
					throw new EntityDatatableCheckNotSupportedException(entity, productId);
				}
				entityDatatableCheck = this.entityDatatableChecksRepository
						.findByEntityStatusAndDatatableIdAndProductId(entity, status, datatableName, productId);
				if (!entityDatatableCheck.isEmpty()) {
					throw new EntityDatatableCheckAlreadyExistsException(entity, status, datatableName, productId);
				}
			}

			final EntityDatatableChecks check = EntityDatatableChecks.fromJson(command);

			this.entityDatatableChecksRepository.saveAndFlush(check);

			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withEntityId(check.getId()) //
					.build();
		} catch (final DataAccessException e) {
			handleReportDataIntegrityIssues(command, e.getMostSpecificCause(), e);
			return CommandProcessingResult.empty();
		}catch (final PersistenceException dve) {
			Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
			handleReportDataIntegrityIssues(command, throwable, dve);
			return CommandProcessingResult.empty();
		}
	}

	public void runTheCheck(final Long entityId, final String entityName, final Long statusCode,
			String foreignKeyColumn) {
		final List<EntityDatatableChecks> tableRequiredBeforeClientActivation = entityDatatableChecksRepository
				.findByEntityAndStatus(entityName, statusCode);

		if (tableRequiredBeforeClientActivation != null) {

			for (EntityDatatableChecks t : tableRequiredBeforeClientActivation) {

				final String datatableName = t.getDatatableName();
				final Long countEntries = readWriteNonCoreDataService.countDatatableEntries(datatableName, entityId,
						foreignKeyColumn);

				logger.info("The are " + countEntries + " entries in the table " + datatableName);
				if (countEntries.intValue() == 0) {
					throw new DatatabaleEntryRequiredException(datatableName);
				}
			}
		}

	}

	public void runTheCheckForProduct(final Long entityId, final String entityName, final Long statusCode,
			String foreignKeyColumn, long productId) {
		List<EntityDatatableChecks> tableRequiredBeforAction = entityDatatableChecksRepository
				.findByEntityStatusAndProduct(entityName, statusCode, productId);

		if (tableRequiredBeforAction == null || tableRequiredBeforAction.size() < 1) {
			tableRequiredBeforAction = entityDatatableChecksRepository.findByEntityStatusAndNoProduct(entityName,
					statusCode);
		}
		if (tableRequiredBeforAction != null) {

			for (EntityDatatableChecks t : tableRequiredBeforAction) {

				final String datatableName = t.getDatatableName();
				final Long countEntries = readWriteNonCoreDataService.countDatatableEntries(datatableName, entityId,
						foreignKeyColumn);

				logger.info("The are " + countEntries + " entries in the table " + datatableName);
				if (countEntries.intValue() == 0) {
					throw new DatatabaleEntryRequiredException(datatableName);
				}
			}
		}

	}

	@Transactional
	@Override
	public CommandProcessingResult deleteCheck(final Long entityDatatableCheckId) {

		final EntityDatatableChecks check = this.entityDatatableChecksRepository.findOne(entityDatatableCheckId);
		if (check == null) {
			throw new EntityDatatableChecksNotFoundException(entityDatatableCheckId);
		}

		this.entityDatatableChecksRepository.delete(check);

		return new CommandProcessingResultBuilder() //
				.withEntityId(entityDatatableCheckId) //
				.build();
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue
	 * is.
	 */
	private void handleReportDataIntegrityIssues(final JsonCommand command, final Throwable realCause,  final Exception dae) {

		if (realCause.getMessage().contains("FOREIGN KEY (`x_registered_table_name`)")) {
			final String datatableName = command.stringValueOfParameterNamed("datatableName");
			throw new PlatformDataIntegrityException("error.msg.entityDatatableCheck.foreign.key.constraint",
					"datatable with name '" + datatableName + "' do not exist", "datatableName", datatableName);
		}

		if (realCause.getMessage().contains("unique_entity_check")) {
			final String datatableName = command.stringValueOfParameterNamed("datatableName");
			final long status = command.longValueOfParameterNamed("status");
			final String entity = command.stringValueOfParameterNamed("entity");
			final long productId = command.longValueOfParameterNamed("productId");
			throw new EntityDatatableCheckAlreadyExistsException(entity, status, datatableName, productId);
		}

		logger.error(dae.getMessage(), dae);
		throw new PlatformDataIntegrityException("error.msg.report.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause.getMessage());
	}

}