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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.cog.propNest.module.tenantOnboardingLease.dto.LeaseAgreementDTO;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.service.LeaseAgreementService;

/**
 * Unit tests for {@link LeaseAgreementController}.
 */
@ExtendWith(MockitoExtension.class)
class LeaseAgreementControllerTest {

    @Mock
    private LeaseAgreementService service;

    @InjectMocks
    private LeaseAgreementController controller;

    @Test
    void createLease_returns201() {
        when(service.createLease(any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.createLease(new LeaseAgreementDTO());
        assertEquals(201, res.getStatusCode().value());
    }

    @Test
    void getAllLeases_returns200() {
        when(service.getAllLeases(null)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getAllLeases(null);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void getLeaseById_returns200() {
        when(service.getLeaseById(1L)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getLeaseById(1L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void activateLease_returns200() {
        when(service.activateLease(1L)).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res = controller.activateLease(1L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void renewLease_returns200() {
        when(service.renewLease(eq(1L), any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.renewLease(1L, Map.of("escalationPercent", 10.0));
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void terminateLease_returns200() {
        when(service.terminateLease(eq(1L), any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.terminateLease(1L, Map.of());
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void getLeaseById_propagatesNotFound() {
        when(service.getLeaseById(anyLong()))
            .thenThrow(new LeaseAgreementNotFoundException("missing"));
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> controller.getLeaseById(99L));
    }
}
