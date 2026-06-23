package com.cog.propNest.module.rentDepositManagement.exception;

/**
 * Thrown when a full refund is requested for a deposit that is not in
 * {@code Held} status. Maps to HTTP 400.
 */
public class DepositNotRefundableException extends BadRequestException {

    public DepositNotRefundableException() {
        super("Deposit must be Held for full refund");
    }
}
