package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a requested lease agreement (or its query result) does not exist.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class LeaseAgreementNotFoundException extends RuntimeException {

    public LeaseAgreementNotFoundException(String message) {
        super(message);
    }
}
