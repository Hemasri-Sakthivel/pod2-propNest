package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the supplied current password does not match during a password
 * change. Maps to HTTP 400.
 */
public class IncorrectPasswordException extends IamException {

    public IncorrectPasswordException() {
        super(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
}
