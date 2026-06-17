package com.cog.propNest.module.rentDepositManagement.exception;

/**
 * Thrown when an attempt is made to modify a payment that has already been
 * marked {@code Received}. Maps to HTTP 400.
 */
public class PaymentAlreadyReceivedException extends BadRequestException {

    public PaymentAlreadyReceivedException() {
        super("Cannot modify a Received payment");
    }
}
