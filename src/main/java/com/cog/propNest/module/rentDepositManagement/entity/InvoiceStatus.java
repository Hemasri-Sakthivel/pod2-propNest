package com.cog.propNest.module.rentDepositManagement.entity;

/**
 * Lifecycle status of a {@link RentInvoice}, mirroring the {@code rent_invoice.status}
 * column in the schema.
 */
public enum InvoiceStatus {
    Generated,
    Sent,
    Paid,
    Overdue,
    Disputed
}
