package com.cog.propNest.module
    .propertyListingPortfolio.exception;

/**
 * Thrown when a unit cannot be found for a given id, tenant or property.
 * Maps to HTTP 404 via the global exception handler.
 */
public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(String message) {
        super(message);
    }

    public UnitNotFoundException(Integer unitID) {
        super("Unit not found with id: " + unitID);
    }
}
