package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when a property with the same name already exists in the same city
 * for the same owner. Maps to HTTP 409.
 */
public class DuplicatePropertyException extends RuntimeException {

    public DuplicatePropertyException(String message) {
        super(message);
    }
}
