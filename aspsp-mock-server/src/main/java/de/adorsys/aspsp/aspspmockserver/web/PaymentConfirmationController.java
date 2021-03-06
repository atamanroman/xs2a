/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.domain.Confirmation;
import de.adorsys.aspsp.aspspmockserver.domain.ConfirmationType;
import de.adorsys.aspsp.aspspmockserver.exception.ApiError;
import de.adorsys.aspsp.aspspmockserver.service.TanConfirmationService;
import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent/confirmation/pis")
@Api(tags = "Payment confirmation for online banking", description = "Provides access to email TAN confirmation for payment execution")
public class PaymentConfirmationController {

    private final TanConfirmationService tanConfirmationService;
    private final PaymentService paymentService;

    @PostMapping
    @ApiOperation(value = "Generates TAN for pis consent confirmation", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    public ResponseEntity generateAndSendTan(Principal principal) {
        return tanConfirmationService.generateAndSendTanForPsuByName(principal.getName())
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @PutMapping
    @ApiOperation(value = "Confirm TAN", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    public ResponseEntity confirmTan(@RequestBody Confirmation confirmation) {
        return paymentService.getPaymentById(confirmation.getPaymentId()).isPresent()
                   ? tanConfirmationService.confirmTan(confirmation.getIban(), confirmation.getTanNumber(), confirmation.getConsentId(), ConfirmationType.PAYMENT)
                   : new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, "PAYMENT_MISSING", "Bad request"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(path = "/{consent-id}/{status}")
    @ApiOperation(value = "Update pis consent status of the corresponding consent", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity updatePisConsentStatus(@PathVariable("consent-id") String consentId,
                                              @PathVariable("status") SpiConsentStatus status) {
        paymentService.updatePaymentConsentStatus(consentId, status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
