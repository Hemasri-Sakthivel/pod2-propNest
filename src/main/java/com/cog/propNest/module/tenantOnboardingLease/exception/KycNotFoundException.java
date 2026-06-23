package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a requested KYC record (or its query result) does not exist.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class KycNotFoundException extends RuntimeException {

    public KycNotFoundException(String message) {
        super(message);
    }
}
