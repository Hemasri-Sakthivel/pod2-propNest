package com.cog.propNest.module.tenantOnboardingLease.exception;

/**
 * Thrown when a single uploaded document exceeds the maximum allowed file size.
 * Mapped to HTTP 413 (Payload Too Large) by the global exception handler.
 */
public class FileSizeLimitExceededException extends RuntimeException {

    public FileSizeLimitExceededException(String message) {
        super(message);
    }
}
