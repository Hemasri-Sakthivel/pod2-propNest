package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when a property cannot be found (missing id or Delisted).
 * Maps to HTTP 404 via the global exception handler.
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(Integer propertyID) {
        super("Property not found with id: " + propertyID);
    }
}
