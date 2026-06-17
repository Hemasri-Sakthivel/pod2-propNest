package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated caller lacks the role required for an endpoint
 * (e.g. a non-admin calling an admin endpoint). Maps to HTTP 403.
 */
public class AccessDeniedException extends IamException {

    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
