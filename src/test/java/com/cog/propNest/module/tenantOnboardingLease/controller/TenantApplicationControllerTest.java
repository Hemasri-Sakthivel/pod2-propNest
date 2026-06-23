package com.cog.propNest.module.tenantOnboardingLease.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantApplicationDTO;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.service.TenantApplicationService;

/**
 * Unit tests for {@link TenantApplicationController}.
 */
@ExtendWith(MockitoExtension.class)
class TenantApplicationControllerTest {

    @Mock
    private TenantApplicationService service;

    @InjectMocks
    private TenantApplicationController controller;

    @Test
    void createTenant_returns201() {
        when(service.createTenant(any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.createTenant(new TenantApplicationDTO());
        assertEquals(201, res.getStatusCode().value());
    }

    @Test
    void getAllTenants_returns200() {
        when(service.getAllTenants(null)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getAllTenants(null);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void getTenantById_returns200() {
        when(service.getTenantById(1L)).thenReturn(Map.of("data", "x"));
        ResponseEntity<Map<String, Object>> res = controller.getTenantById(1L);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void updateTenant_returns200() {
        when(service.updateTenant(eq(1L), any())).thenReturn(Map.of("message", "ok"));
        ResponseEntity<Map<String, Object>> res =
            controller.updateTenant(1L, Map.of("status", "U"));
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void createTenant_propagatesInvalidRequest() {
        when(service.createTenant(any())).thenThrow(new InvalidRequestException("bad"));
        assertThrows(InvalidRequestException.class,
            () -> controller.createTenant(new TenantApplicationDTO()));
    }
}
