package com.cog.propNest.module.maintenanceFacility.controller;

import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
import com.cog.propNest.module.maintenanceFacility.service.MaintenanceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/propNest/maintenanceFacility")
public class MaintenanceRequestController {

    @Autowired
    private MaintenanceRequestService maintenanceRequestService;

    // ── POST: Create Request ──
    @PostMapping("/createRequest")
    public ResponseEntity<Map<String, Object>> createRequest(
            @RequestBody MaintenanceRequest request) {
        Map<String, Object> response = maintenanceRequestService.createRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ── GET: Fetch All Requests ──
    @GetMapping("/fetchAllRequests")
    public ResponseEntity<Map<String, Object>> fetchAllRequests() {
        Map<String, Object> response = maintenanceRequestService.fetchAllRequests();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── GET: Fetch Request By ID ──
    @GetMapping("/fetchRequestById/{requestId}")
    public ResponseEntity<Map<String, Object>> fetchRequestById(
            @PathVariable int requestId) {
        Map<String, Object> response = maintenanceRequestService.fetchRequestById(requestId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── GET: Fetch Requests By Unit ──
    @GetMapping("/fetchRequestsByUnit/{unitId}")
    public ResponseEntity<Map<String, Object>> fetchRequestsByUnit(
            @PathVariable int unitId) {
        Map<String, Object> response = maintenanceRequestService.fetchRequestsByUnit(unitId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── GET: Fetch Requests By Tenant ──
    @GetMapping("/fetchRequestsByTenant/{tenantId}")
    public ResponseEntity<Map<String, Object>> fetchRequestsByTenant(
            @PathVariable int tenantId) {
        Map<String, Object> response = maintenanceRequestService.fetchRequestsByTenant(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Assign Technician ──
    @PutMapping("/assignTechnician/{requestId}")
    public ResponseEntity<Map<String, Object>> assignTechnician(
            @PathVariable int requestId,
            @RequestParam int techId) {
        Map<String, Object> response = maintenanceRequestService.assignTechnician(requestId, techId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Update Status ──
    @PutMapping("/updateStatus/{requestId}")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable int requestId,
            @RequestParam String status) {
        Map<String, Object> response = maintenanceRequestService.updateStatus(requestId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Close Request ──
    @PutMapping("/closeRequest/{requestId}")
    public ResponseEntity<Map<String, Object>> closeRequest(
            @PathVariable int requestId) {
        Map<String, Object> response = maintenanceRequestService.closeRequest(requestId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ── PUT: Reopen Request ──
    @PutMapping("/reopenRequest/{requestId}")
    public ResponseEntity<Map<String, Object>> reopenRequest(
            @PathVariable int requestId) {
        Map<String, Object> response = maintenanceRequestService.reopenRequest(requestId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}