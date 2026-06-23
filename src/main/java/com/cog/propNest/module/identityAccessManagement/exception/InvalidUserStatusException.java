package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a status value outside {@code A}, {@code I}, {@code S} is supplied.
 * Maps to HTTP 400.
 */
public class InvalidUserStatusException extends IamException {

    public InvalidUserStatusException(String value) {
        super(HttpStatus.BAD_REQUEST, "Invalid status value: " + value);
    }
}
