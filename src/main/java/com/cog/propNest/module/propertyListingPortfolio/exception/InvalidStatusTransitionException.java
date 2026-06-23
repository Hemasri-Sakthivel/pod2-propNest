package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when a property or unit is moved to a status that is not allowed
 * from its current status, or when the target status is invalid.
 * Maps to HTTP 409.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
