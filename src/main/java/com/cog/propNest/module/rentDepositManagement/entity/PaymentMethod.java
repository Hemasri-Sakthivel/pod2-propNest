package com.cog.propNest.module.rentDepositManagement.entity;

/**
 * Channel through which a {@link RentPayment} was made, mirroring the
 * {@code rent_payment.method} column in the schema.
 */
public enum PaymentMethod {
    BankTransfer,
    Cheque,
    Cash,
    Online
}
