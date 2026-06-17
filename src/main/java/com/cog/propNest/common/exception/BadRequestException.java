package com.cog.propNest.common.exception;

/**
 * Thrown when the request is syntactically valid but semantically invalid, e.g.
 * an unknown roleId, an illegal status value, or a wrong current password.
 * Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
