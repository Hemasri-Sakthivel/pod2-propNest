package com.cog.propNest.module.maintenanceFacility.entity;

import com.cog.propNest.module.maintenanceFacility.dto.MaintenanceRequestDTO;
import com.cog.propNest.module.maintenanceFacility.dto.WorkOrderDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Lombok-generated accessors and equals/hashCode on the module
 * entities and DTOs.
 */
class MaintenanceFacilityModelTest {

    @Test
    @DisplayName("MaintenanceRequest getters return the values set (incl. null assignedTechId)")
    void maintenanceRequest_gettersSetters() {
        LocalDate raised = LocalDate.of(2024, 3, 5);
        MaintenanceRequest r = new MaintenanceRequest();
        r.setRequestId(1);
        r.setUnitId(2);
        r.setAssignedTechId(null);
        r.setCategory("PL");
        r.setDescription("Leak");
        r.setPriority("HI");
        r.setRaisedDate(raised);
        r.setStatus("OP");

        assertEquals(1, r.getRequestId());
        assertEquals(2, r.getUnitId());
        assertNull(r.getAssignedTechId());
        assertEquals("PL", r.getCategory());
        assertEquals("Leak", r.getDescription());
        assertEquals("HI", r.getPriority());
        assertEquals(raised, r.getRaisedDate());
        assertEquals("OP", r.getStatus());
    }

    @Test
    @DisplayName("MaintenanceRequest equals/hashCode are consistent for equal field sets")
    void maintenanceRequest_equalsHashCode() {
        MaintenanceRequest a = new MaintenanceRequest();
        a.setRequestId(1);
        a.setStatus("OP");
        MaintenanceRequest b = new MaintenanceRequest();
        b.setRequestId(1);
        b.setStatus("OP");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("WorkOrder getters return the values set")
    void workOrder_gettersSetters() {
        LocalDate scheduled = LocalDate.of(2024, 3, 5);
        WorkOrder w = new WorkOrder();
        w.setWorkOrderId(1);
        w.setRequestId(2);
        w.setTechnicianId(3);
        w.setScheduledDate(scheduled);
        w.setWorkDescription("Fix pipe");
        w.setMaterialCost(750.0);
        w.setLabourCost(400.0);
        w.setStatus("SC");

        assertEquals(1, w.getWorkOrderId());
        assertEquals(2, w.getRequestId());
        assertEquals(3, w.getTechnicianId());
        assertEquals(scheduled, w.getScheduledDate());
        assertEquals("Fix pipe", w.getWorkDescription());
        assertEquals(750.0, w.getMaterialCost());
        assertEquals(400.0, w.getLabourCost());
        assertEquals("SC", w.getStatus());
    }

    @Test
    @DisplayName("WorkOrder equals/hashCode are consistent for equal field sets")
    void workOrder_equalsHashCode() {
        WorkOrder a = new WorkOrder();
        a.setWorkOrderId(1);
        a.setStatus("SC");
        WorkOrder b = new WorkOrder();
        b.setWorkOrderId(1);
        b.setStatus("SC");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("MaintenanceRequestDTO getters return the values set")
    void maintenanceRequestDto_gettersSetters() {
        MaintenanceRequestDTO dto = new MaintenanceRequestDTO();
        dto.setRequestId(1);
        dto.setUnitId(2);
        dto.setAssignedTechId(3);
        dto.setCategory("EL");
        dto.setDescription("Power");
        dto.setPriority("EM");
        dto.setStatus("AS");

        assertEquals(1, dto.getRequestId());
        assertEquals(2, dto.getUnitId());
        assertEquals(3, dto.getAssignedTechId());
        assertEquals("EL", dto.getCategory());
        assertEquals("Power", dto.getDescription());
        assertEquals("EM", dto.getPriority());
        assertEquals("AS", dto.getStatus());
    }

    @Test
    @DisplayName("WorkOrderDTO getters return the values set")
    void workOrderDto_gettersSetters() {
        WorkOrderDTO dto = new WorkOrderDTO();
        dto.setWorkOrderId(1);
        dto.setRequestId(2);
        dto.setTechnicianId(3);
        dto.setWorkDescription("Service AC");
        dto.setMaterialCost(1200.0);
        dto.setLabourCost(600.0);
        dto.setStatus("IP");

        assertEquals(1, dto.getWorkOrderId());
        assertEquals(2, dto.getRequestId());
        assertEquals(3, dto.getTechnicianId());
        assertEquals("Service AC", dto.getWorkDescription());
        assertEquals(1200.0, dto.getMaterialCost());
        assertEquals(600.0, dto.getLabourCost());
        assertEquals("IP", dto.getStatus());
    }
}
