package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a refresh token is unknown, expired or has been revoked (e.g.
 * after logout). Maps to HTTP 401.
 */
public class InvalidRefreshTokenException extends IamException {

    public InvalidRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
    }
}
