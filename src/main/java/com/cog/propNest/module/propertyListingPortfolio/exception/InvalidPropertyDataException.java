package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when property request data fails validation (missing required
 * fields, invalid enum value, out-of-range numbers, etc.). Maps to HTTP 400.
 */
public class InvalidPropertyDataException extends RuntimeException {

    public InvalidPropertyDataException(String message) {
        super(message);
    }
}
