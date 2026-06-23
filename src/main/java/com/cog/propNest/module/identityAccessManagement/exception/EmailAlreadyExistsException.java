package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when registering or creating a user with an email that already exists.
 * Maps to HTTP 409.
 */
public class EmailAlreadyExistsException extends IamException {

    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "Email already registered: " + email);
    }
}
