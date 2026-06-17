package com.cog.propNest.module.maintenanceFacility.service;

import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidWorkOrderStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.WorkOrderNotFoundException;
import com.cog.propNest.module.maintenanceFacility.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    // ── POST: Create Work Order ──
    public Map<String, Object> createWorkOrder(WorkOrder workOrder) {
        workOrder.setStatus("SC");
        workOrder.setMaterialCost(0.0);
        workOrder.setLabourCost(0.0);
        WorkOrder saved = workOrderRepository.save(workOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Work order created successfully");
        // response.put("data", saved);
        return response;
    }

    // ── GET: Fetch All Work Orders ──
    public Map<String, Object> fetchAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", workOrders);
        return response;
    }

    // ── GET: Fetch Work Order By ID ──
    public Map<String, Object> fetchWorkOrderById(int workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        Map<String, Object> response = new HashMap<>();
        response.put("data", workOrder);
        return response;
    }

    // ── GET: Fetch Work Orders By Technician ──
    public Map<String, Object> fetchWorkOrdersByTechnician(int techId) {
        List<WorkOrder> workOrders = workOrderRepository.findByTechnicianId(techId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", workOrders);
        return response;
    }

    // ── PUT: Update Work Order Status ──
    // Allowed flow: SC -> IP -> CM, with CN allowed from any state
    public Map<String, Object> updateWorkOrderStatus(int workOrderId, String status) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        String currentStatus = workOrder.getStatus();

        boolean validTransition = (currentStatus.equals("SC") && status.equals("IP"))
                || (currentStatus.equals("IP") && status.equals("CM"))
                || status.equals("CN");

        if (!validTransition) {
            throw new InvalidWorkOrderStatusException(currentStatus, status);
        }

        if (status.equals("IP")) {
            workOrder.setActualVisitDate(LocalDate.now());
        }
        workOrder.setStatus(status);
        workOrderRepository.save(workOrder);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Work order status updated successfully");
        return response;
    }

    // ── PUT: Update Work Order Costs ──
    public Map<String, Object> updateWorkOrderCosts(int workOrderId,
            double materialCost, double labourCost) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        workOrder.setMaterialCost(materialCost);
        workOrder.setLabourCost(labourCost);
        workOrderRepository.save(workOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Work order costs updated successfully");
        return response;
    }

    // ── PUT: Complete Work Order ──
    public Map<String, Object> completeWorkOrder(int workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        workOrder.setStatus("CM");
        if (workOrder.getActualVisitDate() == null) {
            workOrder.setActualVisitDate(LocalDate.now());
        }
        workOrderRepository.save(workOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Work order completed successfully");
        return response;
    }

    // ── PUT: Cancel Work Order (soft delete → CN) ──
    // Only allowed while the work order is still Scheduled (SC); the record is
    // retained in the database for audit and analytics.
    public Map<String, Object> cancelWorkOrder(int workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
        if (!"SC".equals(workOrder.getStatus())) {
            throw new InvalidWorkOrderStatusException(
                    "Work order cannot be cancelled - already " + workOrder.getStatus());
        }
        workOrder.setStatus("CN");
        workOrderRepository.save(workOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Work order cancelled successfully");
        return response;
    }
}
