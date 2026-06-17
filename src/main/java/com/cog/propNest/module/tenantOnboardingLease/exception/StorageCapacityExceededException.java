package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when storing a document would exceed the portal's total storage
 * capacity across all tenants.
 * Mapped to HTTP 507 (Insufficient Storage) by the global exception handler.
 */
public class StorageCapacityExceededException extends RuntimeException {

    public StorageCapacityExceededException(String message) {
        super(message);
    }
}
