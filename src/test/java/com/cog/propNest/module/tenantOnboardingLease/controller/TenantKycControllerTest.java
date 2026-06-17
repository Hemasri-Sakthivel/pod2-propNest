package com.cog.propNest.module.tenantOnboardingLease.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantKycDTO;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.service.TenantKycService;

/**
 * Unit tests for {@link TenantKycController} — verifies the HTTP status codes
 * it returns and that service exceptions propagate to the global handler.
 */
@ExtendWith(MockitoExtension.class)
class TenantKycControllerTest {

    @Mock
    private TenantKycService service;

    @InjectMocks
    private TenantKycController controller;

    @Test
    void createKyc_returns201() {
        when(service.createKyc(any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res = controller.createKyc(new TenantKycDTO());
        assertEquals(201, res.getStatusCode().value());
    }

    @Test
    void getAllKyc_returns200() {
        when(service.getAllKyc(null)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getAllKyc(null);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void getKycById_returns200() {
        when(service.getKycById(1L)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getKycById(1L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void uploadDocument_returns201() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(service.uploadDocument(eq(1L), eq("Passport"), any()))
            .thenReturn(Map.of("message", "KYC document uploaded successfully"));
        ResponseEntity<Map<String, Object>> res =
            controller.uploadDocument(1L, "Passport", file);
        assertEquals(201, res.getStatusCode().value());
    }

    @Test
    void verifyDocument_returns200() {
        when(service.verifyDocument(1L)).thenReturn(Map.of("available", true));
        ResponseEntity<Map<String, Object>> res = controller.verifyDocument(1L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void verifyKyc_returns200() {
        when(service.verifyKyc(eq(1L), any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.verifyKyc(1L, Map.of("status", "V"));
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void createKyc_propagatesInvalidRequest() {
        when(service.createKyc(any())).thenThrow(new InvalidRequestException("bad"));
        assertThrows(InvalidRequestException.class,
            () -> controller.createKyc(new TenantKycDTO()));
    }

    @Test
    void getKycById_propagatesNotFound() {
        when(service.getKycById(anyLong())).thenThrow(new KycNotFoundException("missing"));
        assertThrows(KycNotFoundException.class, () -> controller.getKycById(99L));
    }
}
