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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.consent.api.AccountType;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConsentMapper {

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent consent) {
        return new SpiAccountConsent(
            consent.getId().toString(),
            mapToSpiAccountAccess(consent.getAccounts()),
            consent.isRecurringIndicator(),
            consent.getExpireDate(),
            consent.getUsageCounter(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            false, consent.isTppRedirectPreferred());
    }

    private SpiAccountAccess mapToSpiAccountAccess(List<AisAccount> aisAccounts) {
        return new SpiAccountAccess(mapToSpiAccountReference(aisAccounts, TypeAccess.ACCOUNT),
            mapToSpiAccountReference(aisAccounts, TypeAccess.BALANCE),
            mapToSpiAccountReference(aisAccounts, TypeAccess.TRANSACTION),
            null,
            null);
    }

    private List<SpiAccountReference> mapToSpiAccountReference(List<AisAccount> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .map(acc -> mapToSpiAccountReference(acc, typeAccess))
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private List<SpiAccountReference> mapToSpiAccountReference(AisAccount aisAccount, TypeAccess typeAccess) {
        return aisAccount.getAccesses().stream()
                   .filter(ass -> ass.getTypeAccess() == typeAccess)
                   .map(access -> mapToSpiAccountReference(aisAccount.getAccountTypeId(), aisAccount.getAccountType(), access.getCurrency()))
                   .collect(Collectors.toList());
    }

    private SpiAccountReference mapToSpiAccountReference(String accountTypeId, AccountType accountType, Currency currency) {
        if (accountType == AccountType.IBAN) {
            return new SpiAccountReference(accountTypeId, "", "", "", "", currency);
        } else if (accountType == AccountType.BBAN) {
            return new SpiAccountReference("", accountTypeId, "", "", "", currency);
        } else if (accountType == AccountType.PAN) {
            return new SpiAccountReference("", "", accountTypeId, "", "", currency);
        } else if (accountType == AccountType.MASKEDPAN) {
            return new SpiAccountReference("", "", "", accountTypeId, "", currency);
        } else if (accountType == AccountType.MSISDN) {
            return new SpiAccountReference("", "", "", "", accountTypeId, currency);
        } else {
            log.warn("Unknown account type! {}", accountType);
            return null;
        }
    }
}
