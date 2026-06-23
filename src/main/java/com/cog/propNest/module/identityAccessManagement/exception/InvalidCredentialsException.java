package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when login fails due to a wrong email/password combination. Maps to
 * HTTP 401. The message is intentionally generic to avoid leaking which field
 * was wrong.
 */
public class InvalidCredentialsException extends IamException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
