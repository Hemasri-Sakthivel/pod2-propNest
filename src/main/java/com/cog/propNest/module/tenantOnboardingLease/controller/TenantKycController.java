
package com.cog.propNest.module.tenantOnboardingLease.controller;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantKycDTO;
import com.cog.propNest.module.tenantOnboardingLease.service.TenantKycService;
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
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/propNest/tenantOnboarding")
public class TenantKycController {

    @Autowired
    private TenantKycService service;

    // POST - createKyc
    @PostMapping("/createKyc")
    public ResponseEntity<Map<String, Object>>
        createKyc(
        @RequestBody TenantKycDTO dto) {
        Map<String, Object> response =
            service.createKyc(dto);
        return ResponseEntity.status(201).body(response);
    }

    // GET - getAllKyc
    @GetMapping("/getAllKyc")
    public ResponseEntity<Map<String, Object>>
        getAllKyc(
        @RequestParam(required = false) String status) {
        Map<String, Object> response =
            service.getAllKyc(status);
        return ResponseEntity.status(200).body(response);
    }

    // GET - getKycById
    @GetMapping("/getKycById/{kycId}")
    public ResponseEntity<Map<String, Object>>
        getKycById(
        @PathVariable Long kycId) {
        Map<String, Object> response =
            service.getKycById(kycId);
        return ResponseEntity.status(200).body(response);
    }

    // POST - uploadDocument (multipart: actual PDF file upload)
    @PostMapping("/uploadDocument")
    public ResponseEntity<Map<String, Object>>
        uploadDocument(
        @RequestParam Long tenantId,
        @RequestParam String documentType,
        @RequestParam("file") MultipartFile file) {
        Map<String, Object> response =
            service.uploadDocument(tenantId, documentType, file);
        return ResponseEntity.status(201).body(response);
    }

    // GET - verifyDocument (is the file actually present on the path?)
    @GetMapping("/verifyDocument/{kycId}")
    public ResponseEntity<Map<String, Object>>
        verifyDocument(
        @PathVariable Long kycId) {
        Map<String, Object> response =
            service.verifyDocument(kycId);
        return ResponseEntity.status(200).body(response);
    }

    // PUT - verifyKyc
    @PutMapping("/verifyKyc/{kycId}")
    public ResponseEntity<Map<String, Object>>
        verifyKyc(
        @PathVariable Long kycId,
        @RequestBody Map<String, String> body) {
        Map<String, Object> response =
            service.verifyKyc(kycId, body);
        return ResponseEntity.status(200).body(response);
    }
}