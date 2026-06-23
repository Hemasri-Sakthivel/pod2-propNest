package com.cog.propNest.module.maintenanceFacility.service;

import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidRequestStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.MaintenanceRequestNotFoundException;
import com.cog.propNest.module.maintenanceFacility.repository.MaintenanceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository requestRepository;

    // ── POST: Create Request ──
    public Map<String, Object> createRequest(MaintenanceRequest request) {
        request.setStatus("OP");
        request.setRaisedDate(LocalDate.now());
        MaintenanceRequest saved = requestRepository.save(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Maintenance request created successfully");
        // response.put("data", saved);
        return response;
    }

    // ── GET: Fetch All Requests ──
    public Map<String, Object> fetchAllRequests() {
        List<MaintenanceRequest> requests = requestRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", requests);
        return response;
    }

    // ── GET: Fetch Request By ID ──
    public Map<String, Object> fetchRequestById(int requestId) {
        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(requestId));
        Map<String, Object> response = new HashMap<>();
        response.put("data", request);
        return response;
    }

    // ── GET: Fetch Requests By Unit ──
    public Map<String, Object> fetchRequestsByUnit(int unitId) {
        List<MaintenanceRequest> requests = requestRepository.findByUnitId(unitId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", requests);
        return response;
    }

    // ── GET: Fetch Requests By Tenant (assigned technician) ──
    public Map<String, Object> fetchRequestsByTenant(int tenantId) {
        List<MaintenanceRequest> requests = requestRepository.findByAssignedTechId(tenantId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", requests);
        return response;
    }

    // ── PUT: Assign Technician ──
    public Map<String, Object> assignTechnician(int requestId, int techId) {
        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(requestId));
        request.setAssignedTechId(techId);
        request.setStatus("AS");
        requestRepository.save(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Technician assigned successfully");
        return response;
    }

    // ── PUT: Update Status ──
    // Allowed flow: OP -> AS -> IP -> RS -> CL -> RO
    public Map<String, Object> updateStatus(int requestId, String status) {
        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(requestId));
        String currentStatus = request.getStatus();

        boolean validTransition = (currentStatus.equals("OP") && status.equals("AS"))
                || (currentStatus.equals("AS") && status.equals("IP"))
                || (currentStatus.equals("IP") && status.equals("RS"))
                || (currentStatus.equals("RS") && status.equals("CL"))
                || (currentStatus.equals("CL") && status.equals("RO"));

        if (!validTransition) {
            throw new InvalidRequestStatusException(currentStatus, status);
        }

        if (status.equals("RS")) {
            request.setResolvedDate(LocalDate.now());
        }
        request.setStatus(status);
        requestRepository.save(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Status updated successfully");
        return response;
    }

    // ── PUT: Close Request ──
    public Map<String, Object> closeRequest(int requestId) {
        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(requestId));
        request.setStatus("CL");
        requestRepository.save(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request closed successfully");
        return response;
    }

    // ── PUT: Reopen Request ──
    public Map<String, Object> reopenRequest(int requestId) {
        MaintenanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(requestId));
        request.setStatus("RO");
        requestRepository.save(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request reopened successfully");
        return response;
    }
}
