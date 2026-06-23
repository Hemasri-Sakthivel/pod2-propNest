package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when unit request data fails validation (missing required fields,
 * invalid enum value, out-of-range numbers, bad rent range, etc.).
 * Maps to HTTP 400.
 */
public class InvalidUnitDataException extends RuntimeException {

    public InvalidUnitDataException(String message) {
        super(message);
    }
}
