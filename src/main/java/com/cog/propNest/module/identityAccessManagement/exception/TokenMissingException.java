package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a protected endpoint is called without an access token. Maps to
 * HTTP 401.
 */
public class TokenMissingException extends IamException {

    public TokenMissingException() {
        super(HttpStatus.UNAUTHORIZED, "Token missing or expired");
    }

    public TokenMissingException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
