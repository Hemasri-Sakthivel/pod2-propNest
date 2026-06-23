package com.cog.propNest.module.maintenanceFacility.exception;

import com.cog.propNest.common.exception.InvalidStatusTransitionException;
import com.cog.propNest.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the maintenance-facility module exception classes: their messages
 * and their inheritance from the common base exceptions (which drives the
 * HTTP status mapping in the global handler).
 */
class MaintenanceFacilityExceptionTest {

    @Test
    @DisplayName("MaintenanceRequestNotFoundException(id) builds a message with resource and id")
    void maintenanceRequestNotFound_idMessage() {
        MaintenanceRequestNotFoundException ex = new MaintenanceRequestNotFoundException(5);
        assertTrue(ex.getMessage().contains("Maintenance Request"));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    @DisplayName("MaintenanceRequestNotFoundException is a ResourceNotFoundException (maps to 404)")
    void maintenanceRequestNotFound_isResourceNotFound() {
        assertInstanceOf(ResourceNotFoundException.class, new MaintenanceRequestNotFoundException(1));
    }

    @Test
    @DisplayName("WorkOrderNotFoundException(id) builds a message with resource and id")
    void workOrderNotFound_idMessage() {
        WorkOrderNotFoundException ex = new WorkOrderNotFoundException(8);
        assertTrue(ex.getMessage().contains("Work Order"));
        assertTrue(ex.getMessage().contains("8"));
    }

    @Test
    @DisplayName("WorkOrderNotFoundException is a ResourceNotFoundException (maps to 404)")
    void workOrderNotFound_isResourceNotFound() {
        assertInstanceOf(ResourceNotFoundException.class, new WorkOrderNotFoundException(1));
    }

    @Test
    @DisplayName("InvalidRequestStatusException(from,to) mentions both statuses")
    void invalidRequestStatus_fromToMessage() {
        InvalidRequestStatusException ex = new InvalidRequestStatusException("OP", "CL");
        assertTrue(ex.getMessage().contains("OP"));
        assertTrue(ex.getMessage().contains("CL"));
    }

    @Test
    @DisplayName("InvalidRequestStatusException is an InvalidStatusTransitionException (maps to 409)")
    void invalidRequestStatus_isInvalidStatusTransition() {
        assertInstanceOf(InvalidStatusTransitionException.class,
                new InvalidRequestStatusException("OP", "CL"));
    }

    @Test
    @DisplayName("InvalidWorkOrderStatusException(from,to) mentions both statuses")
    void invalidWorkOrderStatus_fromToMessage() {
        InvalidWorkOrderStatusException ex = new InvalidWorkOrderStatusException("SC", "CM");
        assertTrue(ex.getMessage().contains("SC"));
        assertTrue(ex.getMessage().contains("CM"));
    }

    @Test
    @DisplayName("InvalidWorkOrderStatusException is an InvalidStatusTransitionException (maps to 409)")
    void invalidWorkOrderStatus_isInvalidStatusTransition() {
        assertInstanceOf(InvalidStatusTransitionException.class,
                new InvalidWorkOrderStatusException("SC", "CM"));
    }

    @Test
    @DisplayName("Module exceptions are unchecked (RuntimeException) and throwable")
    void moduleException_isRuntimeAndThrowable() {
        assertInstanceOf(RuntimeException.class, new MaintenanceRequestNotFoundException(1));
        assertThrows(WorkOrderNotFoundException.class, () -> {
            throw new WorkOrderNotFoundException(7);
        });
    }
}
