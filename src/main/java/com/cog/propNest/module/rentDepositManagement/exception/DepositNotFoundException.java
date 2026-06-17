package com.cog.propNest.module.rentDepositManagement.exception;

import com.cog.propNest.common.exception.ResourceNotFoundException;

/**
 * Thrown when a security deposit ledger entry cannot be located by its id.
 * Maps to HTTP 404 with the message {@code "Deposit not found"} via the common
 * handler.
 */
public class DepositNotFoundException extends ResourceNotFoundException {

    public DepositNotFoundException(Object id) {
        super("Deposit not found with id: " + id);
    }

    public DepositNotFoundException() {
        super("Deposit not found");
    }
}
