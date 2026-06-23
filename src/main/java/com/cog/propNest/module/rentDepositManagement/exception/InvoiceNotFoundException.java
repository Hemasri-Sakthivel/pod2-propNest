package com.cog.propNest.module.rentDepositManagement.exception;

import com.cog.propNest.common.exception.ResourceNotFoundException;

/**
 * Thrown when a rent invoice cannot be located by its id. Maps to HTTP 404
 * with the message {@code "Invoice not found"} via the common handler.
 */
public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(Object id) {
        super("Invoice not found with id: " + id);
    }

    public InvoiceNotFoundException() {
        super("Invoice not found");
    }
}
