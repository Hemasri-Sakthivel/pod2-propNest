package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when an uploaded document is missing, empty, or not a genuine PDF.
 * Mapped to HTTP 400 by the global exception handler.
 */
public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
