package com.cog.propNest.module.maintenanceFacility.exception;

import com.cog.propNest.common.exception.ResourceNotFoundException;

/**
 * Thrown when a work order cannot be found for the given id.
 * Inherits from {@link ResourceNotFoundException} so the global handler
 * maps it to HTTP 404.
 */
public class WorkOrderNotFoundException extends ResourceNotFoundException {

    public WorkOrderNotFoundException(int workOrderId) {
        super("Work Order", workOrderId);
    }

    public WorkOrderNotFoundException(String message) {
        super(message);
    }
}
