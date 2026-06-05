package com.cog.propNest.common.exception;

/**
 * Thrown when a caller with the wrong role attempts to access an endpoint.
 * Maps to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
