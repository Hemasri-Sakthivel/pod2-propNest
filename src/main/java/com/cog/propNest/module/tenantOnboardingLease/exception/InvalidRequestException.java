package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a request carries missing or invalid fields (e.g. a required
 * field is absent, an unknown documentType, or an unrecognised status value).
 * Mapped to HTTP 400 by the global exception handler.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
