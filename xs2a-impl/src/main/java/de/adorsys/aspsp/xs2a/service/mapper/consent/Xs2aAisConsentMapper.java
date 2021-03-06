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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.consent.*;
import de.adorsys.psd2.model.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    private final AccountMapper accountMapper;

    public SpiCreateAisConsentRequest mapToSpiCreateAisConsentRequest(CreateConsentReq req, String psuId, String tppId, AspspConsentData aspspConsentData) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       SpiCreateAisConsentRequest aisRequest = new SpiCreateAisConsentRequest();
                       aisRequest.setPsuId(psuId);
                       aisRequest.setTppId(tppId);
                       aisRequest.setFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAccess(mapToSpiAccountAccess(req.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());
                       aisRequest.setAspspConsentData(aspspConsentData.getAspspConsentData());

                       return aisRequest;
                   })
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
                   .map(ac -> new AccountConsent(
                       ac.getId(),
                       mapToAccountAccess(ac.getAccess()),
                       ac.isRecurringIndicator(),
                       ac.getValidUntil(),
                       ac.getFrequencyPerDay(),
                       ac.getLastActionDate(),
                       ConsentStatus.valueOf(ac.getConsentStatus().name()),
                       ac.isWithBalance(),
                       ac.isTppRedirectPreferred()))
                   .orElse(null);
    }

    public Optional<ConsentStatus> mapToConsentStatus(SpiConsentStatus spiConsentStatus) {
        return Optional.ofNullable(spiConsentStatus)
                   .map(status -> ConsentStatus.valueOf(status.name()));
    }

    public ActionStatus mapActionStatusError(MessageErrorCode error, boolean withBalance, TypeAccess access) {
        ActionStatus actionStatus = ActionStatus.FAILURE_ACCOUNT;
        if (error == MessageErrorCode.ACCESS_EXCEEDED) {
            actionStatus = ActionStatus.CONSENT_LIMIT_EXCEEDED;
        } else if (error == MessageErrorCode.CONSENT_EXPIRED) {
            actionStatus = ActionStatus.CONSENT_INVALID_STATUS;
        } else if (error == MessageErrorCode.CONSENT_UNKNOWN_400) {
            actionStatus = ActionStatus.CONSENT_NOT_FOUND;
        } else if (error == MessageErrorCode.CONSENT_INVALID) {
            if (access == TypeAccess.TRANSACTION) {
                actionStatus = ActionStatus.FAILURE_TRANSACTION;
            } else if (access == TypeAccess.BALANCE || withBalance) {
                actionStatus = ActionStatus.FAILURE_BALANCE;
            }
        }
        return actionStatus;
    }

    public SpiUpdateConsentPsuDataReq mapToSpiUpdateConsentPsuDataReq(UpdateConsentPsuDataResponse updatePsuData) {
        return Optional.ofNullable(updatePsuData)
                   .map(data -> {
                       SpiUpdateConsentPsuDataReq request = new SpiUpdateConsentPsuDataReq();
                       request.setPsuId(updatePsuData.getPsuId());
                       request.setConsentId(updatePsuData.getConsentId());
                       request.setAuthorizationId(updatePsuData.getAuthorizationId());
                       request.setAuthenticationMethodId(updatePsuData.getAuthenticationMethodId());
                       request.setScaAuthenticationData(updatePsuData.getScaAuthenticationData());
                       request.setPassword(updatePsuData.getPassword());
                       return request;
                   })
                   .orElse(null);
    }

    private Xs2aAccountAccess mapToAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new Xs2aAccountAccess(
                                accountMapper.mapToAccountReferences(aa.getAccounts()),
                                accountMapper.mapToAccountReferences(aa.getBalances()),
                                accountMapper.mapToAccountReferences(aa.getTransactions()),
                                mapToAccountAccessType(aa.getAvailableAccounts()),
                                mapToAccountAccessType(aa.getAllPsd2()))
                   )
                   .orElse(null);
    }

    private Xs2aAccountAccessType mapToAccountAccessType(SpiAccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> Xs2aAccountAccessType.valueOf(at.name()))
                   .orElse(null);
    }

    private SpiAccountAccess mapToSpiAccountAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa -> {
                       SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
                       spiAccountAccess.setAccounts(accountMapper.mapToSpiAccountReferences(aa.getAccounts()));
                       spiAccountAccess.setBalances(accountMapper.mapToSpiAccountReferences(aa.getBalances()));
                       spiAccountAccess.setTransactions(accountMapper.mapToSpiAccountReferences(aa.getTransactions()));
                       spiAccountAccess.setAvailableAccounts(mapToSpiAccountAccessType(aa.getAvailableAccounts()));
                       spiAccountAccess.setAllPsd2(mapToSpiAccountAccessType(aa.getAllPsd2()));
                       return spiAccountAccess;
                   })
                   .orElse(null);
    }

    private SpiAccountAccessType mapToSpiAccountAccessType(Xs2aAccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> SpiAccountAccessType.valueOf(at.name()))
                   .orElse(null);

    }

    public AccountConsentAuthorization mapToAccountConsentAuthorization(SpiAccountConsentAuthorization spiConsentAuthorization) {
        return Optional.ofNullable(spiConsentAuthorization)
                   .map(conAuth -> {
                       AccountConsentAuthorization consentAuthorization = new AccountConsentAuthorization();

                       consentAuthorization.setId(conAuth.getId());
                       consentAuthorization.setConsentId(conAuth.getConsentId());
                       consentAuthorization.setPsuId(conAuth.getPsuId());
                       consentAuthorization.setScaStatus(ScaStatus.valueOf(conAuth.getScaStatus().name()));
                       consentAuthorization.setAuthenticationMethodId(conAuth.getAuthenticationMethodId());
                       consentAuthorization.setScaAuthenticationData(conAuth.getScaAuthenticationData());
                       consentAuthorization.setPassword(conAuth.getPassword());
                       return consentAuthorization;
                   })
                   .orElse(null);
    }
}
