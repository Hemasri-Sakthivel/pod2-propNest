package com.cog.propNest.module.maintenanceFacility.exception;

import com.cog.propNest.common.exception.ResourceNotFoundException;

/**
 * Thrown when a maintenance request cannot be found for the given id.
 * Inherits from {@link ResourceNotFoundException} so the global handler
 * maps it to HTTP 404.
 */
public class MaintenanceRequestNotFoundException extends ResourceNotFoundException {

    public MaintenanceRequestNotFoundException(int requestId) {
        super("Maintenance Request", requestId);
    }

    public MaintenanceRequestNotFoundException(String message) {
        super(message);
    }
}
