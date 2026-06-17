
package com.cog.propNest.module.tenantOnboardingLease.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantApplicationDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.TenantApplication;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.TenantApplicationNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantApplicationRepository;

@Service
public class TenantApplicationService {

    private static final Logger log =
        LoggerFactory.getLogger(TenantApplicationService.class);

    @Autowired
    private TenantApplicationRepository repo;

    // POST - createTenant
    public Map<String, Object> createTenant(TenantApplicationDTO dto) {
        if (dto.getPropertyId() == null) {
            throw new InvalidRequestException("propertyId is required");
        }
        if (dto.getApplicantName() == null ||
            dto.getApplicantName().isEmpty()) {
            throw new InvalidRequestException("applicantName is required");
        }
        if (dto.getEmail() == null ||
            dto.getEmail().isEmpty()) {
            throw new InvalidRequestException("email is required");
        }
        if (dto.getMonthlyIncome() == null ||
            dto.getMonthlyIncome() <= 0) {
            throw new InvalidRequestException(
                "monthlyIncome must be greater than 0");
        }
        TenantApplication app = new TenantApplication();
        app.setPropertyId(dto.getPropertyId());
        app.setUnitId(dto.getUnitId());
        app.setApplicantName(dto.getApplicantName());
        app.setEmail(dto.getEmail());
        app.setPhone(dto.getPhone());
        app.setNationalIdRef(dto.getNationalIdRef());
        app.setMonthlyIncome(dto.getMonthlyIncome());
        app.setApplicationDate(
            dto.getApplicationDate() != null
            ? dto.getApplicationDate()
            : LocalDate.now());
        app.setStatus(
            TenantApplication.ApplicationStatus.S);
        repo.save(app);
        log.info("Tenant application created (applicationId={}, propertyId={})",
            app.getApplicationId(), app.getPropertyId());

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "Application created successfully");
        return response;
    }

    // GET - getAllTenants
    public Map<String, Object> getAllTenants(
                               String status) {
        log.info("Fetching tenant applications (statusFilter={})", status);
        long startNanos = System.nanoTime();
        List<TenantApplication> list;
        if (status != null && !status.isEmpty()) {
            try {
                list = repo.findByStatus(
                    TenantApplication.ApplicationStatus
                    .valueOf(status));
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException(
                    "Invalid status. Use S, U, A or R");
            }
        } else {
            list = repo.findAll();
        }
        long queryMs = (System.nanoTime() - startNanos) / 1_000_000;
        log.info("Query returned {} application(s) in {} ms (statusFilter={})",
            list.size(), queryMs, status);
        if (list.isEmpty()) {
            throw new TenantApplicationNotFoundException("No applications found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("totalElements", list.size());
        return response;
    }

    // GET - getTenantById
    public Map<String, Object> getTenantById(Long applicationId) {
        Optional<TenantApplication> optional =
            repo.findById(applicationId);
        if (optional.isEmpty()) {
            throw new TenantApplicationNotFoundException("Application not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", optional.get());
        return response;
    }

    // PUT - updateTenant
    public Map<String, Object> updateTenant(Long applicationId,Map<String, String> body) {
        Optional<TenantApplication> optional =
            repo.findById(applicationId);
        if (optional.isEmpty()) {
            throw new TenantApplicationNotFoundException("Application not found");
        }
        TenantApplication app = optional.get();
        String newStatus = body.get("status");
        TenantApplication.ApplicationStatus current =
            app.getStatus();

        boolean valid = false;
        if (current == TenantApplication
            .ApplicationStatus.S &&
            "U".equals(newStatus)) valid = true;
        if (current == TenantApplication
            .ApplicationStatus.U &&
            "A".equals(newStatus)) valid = true;
        if (current == TenantApplication
            .ApplicationStatus.U &&
            "R".equals(newStatus)) valid = true;

        if (!valid) {
            log.warn("Rejected status transition {} -> {} for application {}",
                current, newStatus, applicationId);
            throw new InvalidStatusTransitionException(
                "Status transition not allowed");
        }
        app.setStatus(TenantApplication
            .ApplicationStatus.valueOf(newStatus));
        repo.save(app);
        log.info("Application {} status updated to {}", applicationId, newStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "Application status updated successfully");
        return response;
    }
}