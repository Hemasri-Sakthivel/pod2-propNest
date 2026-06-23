package com.cog.propNest.module.rentDepositManagement.entity;

/**
 * Custody state of a {@link SecurityDepositLedger} record, mirroring the
 * {@code security_deposit_ledger.status} column in the schema.
 */
public enum DepositStatus {
    Held,
    PartiallyRefunded,
    FullyRefunded
}
