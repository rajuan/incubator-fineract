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
package org.apache.fineract.gentera.charge.api;

import org.apache.fineract.gentera.transactions.api.GenteraLoanTransactionsApiResource;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.poi.ss.formula.functions.FinanceLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Path("/gentera/charge")
@Component
@Scope("singleton")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class GenteraChargeApiResource {
    private static final Logger logger = LoggerFactory.getLogger(GenteraLoanTransactionsApiResource.class);

    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<BigDecimal> toApiJsonSerializer;

    private final static MathContext CTX = new MathContext(10, RoundingMode.HALF_EVEN);

    @Autowired
    public GenteraChargeApiResource(final PlatformSecurityContext context,
                                    final ApiRequestParameterHelper apiRequestParameterHelper,
                                    final DefaultToApiJsonSerializer<BigDecimal> toApiJsonSerializer) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Path("/vatfrequency")
    public String groupMeetingTransactions(
            @QueryParam("principal") final BigDecimal principal,
            @QueryParam("interest") final BigDecimal interest,
            @QueryParam("numPayments") final BigDecimal numPayments,
            @QueryParam("payEvery") final BigDecimal payEvery,
            @QueryParam("vat") @DefaultValue("1.16") final BigDecimal vat,
            @QueryParam("daysInYear") @DefaultValue("360") final BigDecimal daysInYear) {

        logger.debug(">>>>>> GENTERA >>>>>> principal      : {}", principal);
        logger.debug(">>>>>> GENTERA >>>>>> days           : {}", daysInYear);
        logger.debug(">>>>>> GENTERA >>>>>> repayments     : {}", numPayments);
        logger.debug(">>>>>> GENTERA >>>>>> vat            : {}", vat);
        logger.debug(">>>>>> GENTERA >>>>>> every (days)   : {}", payEvery);
        logger.debug(">>>>>> GENTERA >>>>>> interest       : {}", interest);

        BigDecimal frequencyRate = interest.divide(vat, CTX).divide(daysInYear, CTX).multiply(payEvery);
        logger.debug(">>>>>> GENTERA >>>>>> freq           : {}", frequencyRate);

        BigDecimal frequencyRateVat = frequencyRate.multiply(vat);
        logger.debug(">>>>>> GENTERA >>>>>> freq VAT       : {}", frequencyRateVat);

        BigDecimal pmt = pmt(frequencyRateVat.divide(BigDecimal.valueOf(100.0), CTX), numPayments, principal).setScale(0, BigDecimal.ROUND_UP);
        logger.debug(">>>>>> GENTERA >>>>>> pmt            : {}", pmt);
        BigDecimal pv = pv(frequencyRateVat.divide(BigDecimal.valueOf(100.0), CTX), numPayments, pmt);
        logger.debug(">>>>>> GENTERA >>>>>> present value  : {}", pv);

        return this.toApiJsonSerializer.serialize(pv.setScale(2, BigDecimal.ROUND_HALF_EVEN).abs());
    }

    private BigDecimal pmt(BigDecimal rate, BigDecimal nper, BigDecimal pmt)
    {
        return rate.divide(BigDecimal.ONE.subtract(BigDecimal.ONE.add(rate).pow(nper.negate().intValue(), CTX)), CTX).multiply(pmt);
    }

    private BigDecimal pv(BigDecimal rate, BigDecimal nper, BigDecimal pmt)
    {
        // return pmt.divide(BigDecimal.ONE.add(rate).pow(nper.negate().intValue(), CTX), CTX);
        return BigDecimal.valueOf(FinanceLib.pv(rate.doubleValue(), nper.doubleValue(), pmt.doubleValue(), 0, false));
    }
}