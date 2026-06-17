


package com.cog.propNest.module.tenantOnboardingLease.controller;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantApplicationDTO;
import com.cog.propNest.module.tenantOnboardingLease.service.TenantApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/propNest/tenantOnboarding")
public class TenantApplicationController {

    @Autowired
    private TenantApplicationService service;

    // POST - createTenant
    @PostMapping("/createTenant")
    public ResponseEntity<Map<String, Object>>
        createTenant(
        @RequestBody TenantApplicationDTO dto) {
        Map<String, Object> response =
            service.createTenant(dto);
        return ResponseEntity.status(201).body(response);
    }

    // GET - getAllTenants
    @GetMapping("/getAllTenants")
    public ResponseEntity<Map<String, Object>>
        getAllTenants(
        @RequestParam(required = false) String status) {
        Map<String, Object> response =
            service.getAllTenants(status);
        return ResponseEntity.status(200).body(response);
    }

    // GET - getTenantById
    @GetMapping("/getTenantById/{applicationId}")
    public ResponseEntity<Map<String, Object>>
        getTenantById(
        @PathVariable Long applicationId) {
        Map<String, Object> response =
            service.getTenantById(applicationId);
        return ResponseEntity.status(200).body(response);
    }

    // PUT - updateTenant
    @PutMapping("/updateTenant/{applicationId}")
    public ResponseEntity<Map<String, Object>>
        updateTenant(
        @PathVariable Long applicationId,
        @RequestBody Map<String, String> body) {
        Map<String, Object> response =
            service.updateTenant(applicationId, body);
        return ResponseEntity.status(200).body(response);
    }
}