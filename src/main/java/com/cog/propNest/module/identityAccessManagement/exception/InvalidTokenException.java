package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an access token is malformed, expired or has a bad signature.
 * Maps to HTTP 401.
 */
public class InvalidTokenException extends IamException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
