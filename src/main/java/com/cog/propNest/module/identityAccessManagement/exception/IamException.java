package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all Identity &amp; Access Management exceptions. Each subclass
 * carries the HTTP status it maps to, so a single advice
 * ({@code IamExceptionHandler}) can translate any of them into the standard
 * error response.
 */
public abstract class IamException extends RuntimeException {

    private final HttpStatus status;

    protected IamException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
