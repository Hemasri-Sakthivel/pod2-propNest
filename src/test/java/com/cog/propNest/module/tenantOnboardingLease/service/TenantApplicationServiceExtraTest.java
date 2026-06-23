package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.cog.propNest.module.tenantOnboardingLease.exception.TenantApplicationNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantApplicationRepository;

/**
 * Additional edge-case coverage for {@link TenantApplicationService}.
 */
@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceExtraTest {

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

    @Test
    void createTenant_missingName_throwsInvalidRequest() {
        TenantApplicationDTO dto = validDto();
        dto.setApplicantName("");
        assertThrows(InvalidRequestException.class, () -> service.createTenant(dto));
    }

    @Test
    void createTenant_missingEmail_throwsInvalidRequest() {
        TenantApplicationDTO dto = validDto();
        dto.setEmail(null);
        assertThrows(InvalidRequestException.class, () -> service.createTenant(dto));
    }

    @Test
    void getAllTenants_returnsRecords() {
        when(repo.findAll()).thenReturn(List.of(new TenantApplication()));
        Map<String, Object> res = service.getAllTenants(null);
        assertEquals(1, res.get("totalElements"));
    }

    @Test
    void getTenantById_found() {
        TenantApplication app = new TenantApplication();
        app.setApplicationId(3L);
        when(repo.findById(3L)).thenReturn(Optional.of(app));
        Map<String, Object> res = service.getTenantById(3L);
        assertSame(app, res.get("data"));
    }

    @Test
    void getTenantById_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TenantApplicationNotFoundException.class,
            () -> service.getTenantById(99L));
    }

    @Test
    void updateTenant_underReviewToApproved_success() {
        TenantApplication app = new TenantApplication();
        app.setStatus(TenantApplication.ApplicationStatus.U);
        when(repo.findById(1L)).thenReturn(Optional.of(app));

        Map<String, String> body = new HashMap<>();
        body.put("status", "A");

        service.updateTenant(1L, body);

        assertEquals(TenantApplication.ApplicationStatus.A, app.getStatus());
        verify(repo).save(app);
    }
}
