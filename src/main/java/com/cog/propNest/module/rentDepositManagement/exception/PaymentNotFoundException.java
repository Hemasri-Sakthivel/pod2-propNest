package com.cog.propNest.module.rentDepositManagement.exception;

import com.cog.propNest.common.exception.ResourceNotFoundException;

/**
 * Thrown when a rent payment cannot be located by its id. Maps to HTTP 404
 * with the message {@code "Payment not found"} via the common handler.
 */
public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(Object id) {
        super("Payment not found with id: " + id);
    }

    public PaymentNotFoundException() {
        super("Payment not found");
    }
}
