package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a tenant tries to upload more documents than the per-tenant limit.
 * Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class UploadLimitExceededException extends RuntimeException {

    public UploadLimitExceededException(String message) {
        super(message);
    }
}
