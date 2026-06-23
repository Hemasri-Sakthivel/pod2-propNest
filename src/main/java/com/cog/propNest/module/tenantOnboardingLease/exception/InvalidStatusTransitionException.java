package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when an entity's status cannot move to the requested status
 * (e.g. verifying a KYC that is not Pending, activating a lease not in Draft).
 * Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
