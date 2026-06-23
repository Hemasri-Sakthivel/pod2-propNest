package com.cog.propNest.module.rentDepositManagement.entity;

/**
 * Settlement state of a {@link RentPayment}, mirroring the
 * {@code rent_payment.status} column in the schema.
 */
public enum PaymentStatus {
    Received,
    Partial,
    Failed
}
