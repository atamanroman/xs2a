/**
 * XS2A SPI MOCK API
 * Mock server to simulate ASPSP
 *
 * OpenAPI spec version: 1.0
 * Contact: dgo@adorsys.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import { SpiAccountReference } from './spiAccountReference';
import { SpiAddress } from './spiAddress';
import { SpiAmount } from './spiAmount';
import { SpiRemittance } from './spiRemittance';


export interface AspspPayment {
    creditorAccount?: SpiAccountReference;
    creditorAddress?: SpiAddress;
    creditorAgent?: string;
    creditorName?: string;
    dayOfExecution?: number;
    debtorAccount?: SpiAccountReference;
    endDate?: string;
    endToEndIdentification?: string;
    executionRule?: string;
    frequency?: string;
    instructedAmount?: SpiAmount;
    paymentId?: string;
    paymentStatus?: AspspPayment.PaymentStatusEnum;
    pisPaymentType?: AspspPayment.PisPaymentTypeEnum;
    purposeCode?: string;
    remittanceInformationStructured?: SpiRemittance;
    remittanceInformationUnstructured?: string;
    requestedExecutionDate?: string;
    requestedExecutionTime?: Date;
    startDate?: string;
    ultimateCreditor?: string;
    ultimateDebtor?: string;
}
export namespace AspspPayment {
    export type PaymentStatusEnum = 'ACCP' | 'ACSC' | 'ACSP' | 'ACTC' | 'ACWC' | 'ACWP' | 'RCVD' | 'PDNG' | 'RJCT';
    export const PaymentStatusEnum = {
        ACCP: 'ACCP' as PaymentStatusEnum,
        ACSC: 'ACSC' as PaymentStatusEnum,
        ACSP: 'ACSP' as PaymentStatusEnum,
        ACTC: 'ACTC' as PaymentStatusEnum,
        ACWC: 'ACWC' as PaymentStatusEnum,
        ACWP: 'ACWP' as PaymentStatusEnum,
        RCVD: 'RCVD' as PaymentStatusEnum,
        PDNG: 'PDNG' as PaymentStatusEnum,
        RJCT: 'RJCT' as PaymentStatusEnum
    }
    export type PisPaymentTypeEnum = 'BULK' | 'PERIODIC' | 'FUTURE_DATED' | 'SINGLE';
    export const PisPaymentTypeEnum = {
        BULK: 'BULK' as PisPaymentTypeEnum,
        PERIODIC: 'PERIODIC' as PisPaymentTypeEnum,
        FUTUREDATED: 'FUTURE_DATED' as PisPaymentTypeEnum,
        SINGLE: 'SINGLE' as PisPaymentTypeEnum
    }
}
