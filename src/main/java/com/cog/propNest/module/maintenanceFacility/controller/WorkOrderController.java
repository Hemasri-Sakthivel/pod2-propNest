package com.cog.propNest.module.maintenanceFacility.controller;

import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import com.cog.propNest.module.maintenanceFacility.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/propNest/maintenanceFacility")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    // ── POST: Create Work Order ──
    @PostMapping("/createWorkOrder")
    public ResponseEntity<Map<String, Object>> createWorkOrder(
            @RequestBody WorkOrder workOrder) {
        Map<String, Object> response = workOrderService.createWorkOrder(workOrder);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ── GET: Fetch All Work Orders ──
    @GetMapping("/fetchAllWorkOrders")
    public ResponseEntity<Map<String, Object>> fetchAllWorkOrders() {
        Map<String, Object> response = workOrderService.fetchAllWorkOrders();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── GET: Fetch Work Order By ID ──
    @GetMapping("/fetchWorkOrderById/{workOrderId}")
    public ResponseEntity<Map<String, Object>> fetchWorkOrderById(
            @PathVariable int workOrderId) {
        Map<String, Object> response = workOrderService.fetchWorkOrderById(workOrderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── GET: Fetch Work Orders By Technician ──
    @GetMapping("/fetchWorkOrdersByTechnician/{techId}")
    public ResponseEntity<Map<String, Object>> fetchWorkOrdersByTechnician(
            @PathVariable int techId) {
        Map<String, Object> response = workOrderService.fetchWorkOrdersByTechnician(techId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Update Work Order Status ──
    @PutMapping("/updateWorkOrderStatus/{workOrderId}")
    public ResponseEntity<Map<String, Object>> updateWorkOrderStatus(
            @PathVariable int workOrderId,
            @RequestParam String status) {
        Map<String, Object> response = workOrderService.updateWorkOrderStatus(workOrderId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Update Work Order Costs ──
    @PutMapping("/updateWorkOrderCosts/{workOrderId}")
    public ResponseEntity<Map<String, Object>> updateWorkOrderCosts(
            @PathVariable int workOrderId,
            @RequestParam double materialCost,
            @RequestParam double labourCost) {
        Map<String, Object> response = workOrderService.updateWorkOrderCosts(workOrderId, materialCost, labourCost);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Complete Work Order ──
    @PutMapping("/completeWorkOrder/{workOrderId}")
    public ResponseEntity<Map<String, Object>> completeWorkOrder(
            @PathVariable int workOrderId) {
        Map<String, Object> response = workOrderService.completeWorkOrder(workOrderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Cancel Work Order (soft delete → CN) ──
    @PutMapping("/cancelWorkOrder/{workOrderId}")
    public ResponseEntity<Map<String, Object>> cancelWorkOrder(
            @PathVariable int workOrderId) {
        Map<String, Object> response = workOrderService.cancelWorkOrder(workOrderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}