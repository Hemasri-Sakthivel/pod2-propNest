package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user cannot be found by id. Maps to HTTP 404.
 */
public class UserNotFoundException extends IamException {

    public UserNotFoundException(Long userId) {
        super(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
