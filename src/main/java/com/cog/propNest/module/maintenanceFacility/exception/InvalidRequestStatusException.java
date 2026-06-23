package com.cog.propNest.module.maintenanceFacility.exception;

import com.cog.propNest.common.exception.InvalidStatusTransitionException;

/**
 * Thrown when a maintenance request is moved to a status that is not allowed
 * from its current status. Allowed flow: OP -> AS -> IP -> RS -> CL -> RO.
 * Inherits from {@link InvalidStatusTransitionException} so the global handler
 * maps it to HTTP 409.
 */
public class InvalidRequestStatusException extends InvalidStatusTransitionException {

    public InvalidRequestStatusException(String from, String to) {
        super("Maintenance request status cannot change from '" + from + "' to '" + to + "'");
    }

    public InvalidRequestStatusException(String message) {
        super(message);
    }
}
