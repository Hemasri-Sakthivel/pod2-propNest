package com.cog.propNest.module.identityAccessManagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request references a roleId that does not exist. Maps to
 * HTTP 400.
 */
public class InvalidRoleException extends IamException {

    public InvalidRoleException(Integer roleId) {
        super(HttpStatus.BAD_REQUEST, "Invalid roleId: " + roleId);
    }

    public InvalidRoleException() {
        super(HttpStatus.BAD_REQUEST, "Invalid roleId");
    }
}
