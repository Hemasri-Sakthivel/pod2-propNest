package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantApplicationDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.TenantApplication;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.TenantApplicationNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantApplicationRepository;

/**
 * Unit tests for {@link TenantApplicationService}.
 */
@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceTest {

    @Mock
    private TenantApplicationRepository repo;

    @InjectMocks
    private TenantApplicationService service;

    private TenantApplicationDTO validDto() {
        TenantApplicationDTO dto = new TenantApplicationDTO();
        dto.setPropertyId(1);
        dto.setApplicantName("John Doe");
        dto.setEmail("john@example.com");
        dto.setMonthlyIncome(50000.0);
        return dto;
    }

    // 17
    @Test
    void createTenant_success() {
        Map<String, Object> res = service.createTenant(validDto());
        assertEquals("Application created successfully", res.get("message"));
        verify(repo).save(any(TenantApplication.class));
    }

    // 18
    @Test
    void createTenant_nullPropertyId_throwsInvalidRequest() {
        TenantApplicationDTO dto = validDto();
        dto.setPropertyId(null);
        assertThrows(InvalidRequestException.class, () -> service.createTenant(dto));
    }

    // 19
    @Test
    void createTenant_invalidIncome_throwsInvalidRequest() {
        TenantApplicationDTO dto = validDto();
        dto.setMonthlyIncome(0.0);
        assertThrows(InvalidRequestException.class, () -> service.createTenant(dto));
    }

    // 20
    @Test
    void getAllTenants_empty_throwsNotFound() {
        when(repo.findAll()).thenReturn(List.of());
        assertThrows(TenantApplicationNotFoundException.class,
            () -> service.getAllTenants(null));
    }

    // 21
    @Test
    void updateTenant_validTransition_success() {
        TenantApplication app = new TenantApplication();
        app.setStatus(TenantApplication.ApplicationStatus.S);
        when(repo.findById(1L)).thenReturn(Optional.of(app));

        Map<String, String> body = new HashMap<>();
        body.put("status", "U");

        Map<String, Object> res = service.updateTenant(1L, body);

        assertEquals("Application status updated successfully", res.get("message"));
        assertEquals(TenantApplication.ApplicationStatus.U, app.getStatus());
        verify(repo).save(app);
    }

    // 22
    @Test
    void updateTenant_invalidTransition_throws() {
        TenantApplication app = new TenantApplication();
        app.setStatus(TenantApplication.ApplicationStatus.S);
        when(repo.findById(1L)).thenReturn(Optional.of(app));

        Map<String, String> body = new HashMap<>();
        body.put("status", "A"); // S -> A is not allowed (only S -> U)

        assertThrows(InvalidStatusTransitionException.class,
            () -> service.updateTenant(1L, body));
    }
}
