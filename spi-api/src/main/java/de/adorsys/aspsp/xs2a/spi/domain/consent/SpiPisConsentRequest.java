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

package de.adorsys.aspsp.xs2a.spi.domain.consent;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTppInfo;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import lombok.Data;

import java.util.List;

@Data
public class SpiPisConsentRequest {
    private List<SpiSinglePayment> payments;
    private String paymentProduct;
    private SpiPaymentType paymentType;
    private SpiTppInfo tppInfo;
    private AspspConsentData aspspConsentData;
}
