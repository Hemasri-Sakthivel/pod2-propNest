
package com.cog.propNest.module.tenantOnboardingLease.controller;

import com.cog.propNest.module.tenantOnboardingLease.dto.LeaseAgreementDTO;
import com.cog.propNest.module.tenantOnboardingLease.service.LeaseAgreementService;
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
public class LeaseAgreementController {

    @Autowired
    private LeaseAgreementService service;

    // POST - createLease
    @PostMapping("/createLease")
    public ResponseEntity<Map<String, Object>>
        createLease(
        @RequestBody LeaseAgreementDTO dto) {
        Map<String, Object> response =
            service.createLease(dto);
        return ResponseEntity.status(201).body(response);
    }

    // GET - getAllLeases
    @GetMapping("/getAllLeases")
    public ResponseEntity<Map<String, Object>>
        getAllLeases(
        @RequestParam(required = false) String status) {
        Map<String, Object> response =
            service.getAllLeases(status);
        return ResponseEntity.status(200).body(response);
    }

    // GET - getLeaseById
    @GetMapping("/getLeaseById/{leaseId}")
    public ResponseEntity<Map<String, Object>>
        getLeaseById(
        @PathVariable Long leaseId) {
        Map<String, Object> response =
            service.getLeaseById(leaseId);
        return ResponseEntity.status(200).body(response);
    }

    // PUT - activateLease
    @PutMapping("/activateLease/{leaseId}")
    public ResponseEntity<Map<String, Object>>
        activateLease(
        @PathVariable Long leaseId) {
        Map<String, Object> response =
            service.activateLease(leaseId);
        return ResponseEntity.status(200).body(response);
    }

    // PUT - renewLease
    @PutMapping("/renewLease/{leaseId}")
    public ResponseEntity<Map<String, Object>>
        renewLease(
        @PathVariable Long leaseId,
        @RequestBody Map<String, Object> body) {
        Map<String, Object> response =
            service.renewLease(leaseId, body);
        return ResponseEntity.status(200).body(response);
    }

    // PUT - terminateLease
    @PutMapping("/terminateLease/{leaseId}")
    public ResponseEntity<Map<String, Object>>
        terminateLease(
        @PathVariable Long leaseId,
        @RequestBody Map<String, String> body) {
        Map<String, Object> response =
            service.terminateLease(leaseId, body);
        return ResponseEntity.status(200).body(response);
    }
}