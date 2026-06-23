package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when a unit number already exists within the same property.
 * Maps to HTTP 409.
 */
public class DuplicateUnitException extends RuntimeException {

    public DuplicateUnitException(String message) {
        super(message);
    }
}
