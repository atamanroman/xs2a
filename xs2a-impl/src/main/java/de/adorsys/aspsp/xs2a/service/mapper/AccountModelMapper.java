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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class AccountModelMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static AccountList mapToAccountList(Map<String, List<Xs2aAccountDetails>> accountDetailsList) {
        List<AccountDetails> details = accountDetailsList.values().stream()
                                           .flatMap(ad -> ad.stream().map(AccountModelMapper::mapToAccountDetails))
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public static AccountDetails mapToAccountDetails(Xs2aAccountDetails accountDetails) {
        AccountDetails target = new AccountDetails();
        BeanUtils.copyProperties(accountDetails, target);

        // TODO fill missing values: product status usage details
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        target.resourceId(accountDetails.getId())
            .currency(accountDetails.getCurrency().getCurrencyCode())
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType())
                                 .map(Enum::name)
                                 .orElse(null));
        return target
                   .balances(mapToBalanceList(accountDetails.getBalances()))
                   ._links(OBJECT_MAPPER.convertValue(accountDetails.getLinks(), Map.class));
    }

    private static BalanceList mapToBalanceList(List<Xs2aBalance> balances) {
        BalanceList balanceList = null;

        if (CollectionUtils.isNotEmpty(balances)) {
            balanceList = new BalanceList();

            balanceList.addAll(balances.stream()
                                   .map(AccountModelMapper::mapToBalance)
                                   .collect(Collectors.toList()));
        }

        return balanceList;
    }

    public static ReadBalanceResponse200 mapToBalance(List<Xs2aBalance> balances) {
        BalanceList balancesResponse = new BalanceList();
        balances.forEach(balance -> balancesResponse.add(mapToBalance(balance)));

        return new ReadBalanceResponse200()
                   .balances(balancesResponse);
    }

    public static Balance mapToBalance(Xs2aBalance balance) {
        Balance target = new Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(AmountModelMapper.mapToAmount(balance.getBalanceAmount()));

        Optional.ofNullable(balance.getBalanceType())
            .ifPresent(balanceType -> target.setBalanceType(BalanceType.fromValue(balanceType.getValue())));

        Optional.ofNullable(balance.getLastChangeDateTime())
            .ifPresent(lastChangeDateTime -> {
                List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(lastChangeDateTime);
                target.setLastChangeDateTime(lastChangeDateTime.atOffset(validOffsets.get(0)));
            });

        return target;
    }

    public static AccountReport mapToAccountReport(Xs2aAccountReport accountReport) {
        TransactionList booked = new TransactionList();
        List<TransactionDetails> bookedTransactions = Optional.ofNullable(accountReport.getBooked())
                                                          .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
                                                          .orElse(new ArrayList<>());
        booked.addAll(bookedTransactions);

        TransactionList pending = new TransactionList();
        List<TransactionDetails> pendingTransactions = Optional.ofNullable(accountReport.getPending())
                                                           .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
                                                           .orElse(new ArrayList<>());
        pending.addAll(pendingTransactions);

        return new AccountReport()
                   .booked(booked)
                   .pending(pending)
                   ._links(OBJECT_MAPPER.convertValue(accountReport.getLinks(), Map.class));
    }

    public static TransactionDetails mapToTransaction(Transactions transactions) {
        TransactionDetails target = new TransactionDetails();
        BeanUtils.copyProperties(transactions, target);

        target.setCreditorAccount(createAccountObject(transactions.getCreditorAccount()));
        target.setDebtorAccount(createAccountObject(transactions.getDebtorAccount()));

        // TODO fill missing values: entryReference checkId exchangeRate proprietaryBankTransactionCode links
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        Optional.ofNullable(transactions.getAmount())
            .ifPresent(amount -> target.setTransactionAmount(AmountModelMapper.mapToAmount(amount)));

        target.setPurposeCode(PurposeCode.fromValue(Optional.ofNullable(transactions.getPurposeCode())
                                                        .map(Xs2aPurposeCode::getCode)
                                                        .orElse(null)));

        Optional.ofNullable(transactions.getBankTransactionCodeCode())
            .ifPresent(transactionCode -> target.setBankTransactionCode(transactionCode.getCode()));

        return target;
    }

    public static <T> T mapToAccountReference12(AccountReference reference) {
        T accountReference = null;

        if (StringUtils.isNotBlank(reference.getIban())) {
            accountReference = (T) new AccountReferenceIban().iban(reference.getIban());
            ((AccountReferenceIban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getBban())) {
            accountReference = (T) new AccountReferenceBban().bban(reference.getBban());
            ((AccountReferenceBban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getPan())) {
            accountReference = (T) new AccountReferencePan().pan(reference.getPan());
            ((AccountReferencePan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMaskedPan())) {
            accountReference = (T) new AccountReferenceMaskedPan().maskedPan(reference.getMaskedPan());
            ((AccountReferenceMaskedPan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMsisdn())) {
            accountReference = (T) new AccountReferenceMsisdn().msisdn(reference.getMsisdn());
            ((AccountReferenceMsisdn) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        }
        return accountReference;
    }

    public static Address mapToAddress12(Xs2aAddress address) {
        Address targetAddress = new Address().street(address.getStreet());
        targetAddress.setStreet(address.getStreet());
        targetAddress.setBuildingNumber(address.getBuildingNumber());
        targetAddress.setCity(address.getCity());
        targetAddress.setPostalCode(address.getPostalCode());
        targetAddress.setCountry(address.getCountry().getCode());
        return targetAddress;
    }

    public static Xs2aAddress mapToXs2aAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> {
                       Xs2aAddress targetAddress = new Xs2aAddress();
                       targetAddress.setStreet(a.getStreet());
                       targetAddress.setBuildingNumber(a.getBuildingNumber());
                       targetAddress.setCity(a.getCity());
                       targetAddress.setPostalCode(a.getPostalCode());
                       Xs2aCountryCode code = new Xs2aCountryCode();
                       code.setCode(a.getCountry());
                       targetAddress.setCountry(code);
                       return targetAddress;
                   })
                   .orElse(new Xs2aAddress());
    }

    public static Xs2aAmount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Xs2aAmount targetAmount = new Xs2aAmount();
                       targetAmount.setAmount(a.getAmount());
                       targetAmount.setCurrency(Currency.getInstance(a.getCurrency()));
                       return targetAmount;
                   })
                   .orElse(new Xs2aAmount());

    }

    private static Object createAccountObject(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
                   .map(account -> {
                       if (account.getIban() != null) {
                           return new AccountReferenceIban()
                                      .iban(accountReference.getIban())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getBban() != null) {
                           return new AccountReferenceBban()
                                      .bban(accountReference.getBban())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getPan() != null) {
                           return new AccountReferencePan()
                                      .pan(accountReference.getPan())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getMsisdn() != null) {
                           return new AccountReferenceMsisdn()
                                      .msisdn(accountReference.getMsisdn())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getMaskedPan() != null) {
                           return new AccountReferenceMaskedPan()
                                      .maskedPan(accountReference.getMaskedPan())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       }

                       return null;
                   })
                   .orElse(null);
    }

    private static String getCurrencyFromAccountReference(AccountReference accountReference) {
        return Optional.ofNullable(accountReference.getCurrency())
                   .map(Currency::getCurrencyCode)
                   .orElse(null);
    }
}
