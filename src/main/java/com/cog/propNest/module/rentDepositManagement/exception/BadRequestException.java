package com.cog.propNest.module.rentDepositManagement.exception;

/**
 * Thrown when a request to the Rent &amp; Deposit module fails validation, e.g. a
 * required field is missing or a business rule is broken. Maps to HTTP 400.
 *
 * <p>Extends {@link IllegalArgumentException} so it is picked up by the
 * {@code GlobalExceptionHandler}'s bad-request mapping.</p>
 */
public class BadRequestException extends IllegalArgumentException {

    public BadRequestException(String message) {
        super(message);
    }
}
