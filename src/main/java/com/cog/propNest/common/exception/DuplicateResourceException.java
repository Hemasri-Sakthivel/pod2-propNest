package com.cog.propNest.common.exception;

/**
 * Thrown when a unique constraint is violated, e.g. duplicate email or unit
 * number already exists. Maps to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
