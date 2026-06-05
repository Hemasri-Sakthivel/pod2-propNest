package com.cog.propNest.common.exception;

/**
 * Thrown when an entity is moved to a status that is not allowed from its
 * current status. Maps to HTTP 409.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
