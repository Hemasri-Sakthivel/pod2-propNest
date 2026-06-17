package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a requested tenant application (or its query result) does not exist.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class TenantApplicationNotFoundException extends RuntimeException {

    public TenantApplicationNotFoundException(String message) {
        super(message);
    }
}
