package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user whose account is suspended ({@code S}) or inactive
 * ({@code I}) attempts to log in. Maps to HTTP 401.
 */
public class AccountNotActiveException extends IamException {

    public AccountNotActiveException() {
        super(HttpStatus.UNAUTHORIZED, "Account is not active");
    }
}
