package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a document cannot be written to / read from the server's storage
 * (e.g. an I/O failure while saving the file).
 * Mapped to HTTP 500 by the global exception handler.
 */
public class DocumentStorageException extends RuntimeException {

    public DocumentStorageException(String message) {
        super(message);
    }

    public DocumentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
