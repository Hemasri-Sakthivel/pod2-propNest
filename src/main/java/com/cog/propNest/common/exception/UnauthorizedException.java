package com.cog.propNest.common.exception;

/**
 * Thrown when authentication fails: missing/invalid/expired token or bad
 * credentials. Maps to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
