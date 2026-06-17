package com.cog.propNest.module.maintenanceFacility.exception;

import com.cog.propNest.common.exception.InvalidStatusTransitionException;

/**
 * Thrown when a work order is moved to a status that is not allowed from its
 * current status. Allowed flow: SC -> IP -> CM, with CN allowed from any state.
 * Inherits from {@link InvalidStatusTransitionException} so the global handler
 * maps it to HTTP 409.
 */
public class InvalidWorkOrderStatusException extends InvalidStatusTransitionException {

    public InvalidWorkOrderStatusException(String from, String to) {
        super("Work order status cannot change from '" + from + "' to '" + to + "'");
    }

    public InvalidWorkOrderStatusException(String message) {
        super(message);
    }
}
