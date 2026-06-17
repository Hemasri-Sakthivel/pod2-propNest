package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cog.propNest.module.tenantOnboardingLease.dto.LeaseAgreementDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.LeaseAgreementRepository;

/**
 * Unit tests for {@link LeaseAgreementService}.
 */
@ExtendWith(MockitoExtension.class)
class LeaseAgreementServiceTest {

    @Mock
    private LeaseAgreementRepository repo;

    @InjectMocks
    private LeaseAgreementService service;

    private LeaseAgreementDTO validDto() {
        LeaseAgreementDTO dto = new LeaseAgreementDTO();
        dto.setUnitId(1);
        dto.setTenantId(1L);
        dto.setOwnerId(2L);
        dto.setStartDate(LocalDate.of(2024, 2, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 31));
        dto.setMonthlyRent(25000.0);
        dto.setSecurityDeposit(50000.0);
        return dto;
    }

    // 23
    @Test
    void createLease_success() {
        Map<String, Object> res = service.createLease(validDto());
        assertEquals("Lease created successfully", res.get("message"));
        verify(repo).save(any(LeaseAgreement.class));
    }

    // 24
    @Test
    void getAllLeases_empty_throwsNotFound() {
        when(repo.findAll()).thenReturn(List.of());
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> service.getAllLeases(null));
    }

    // 25
    @Test
    void getLeaseById_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> service.getLeaseById(99L));
    }

    // 26
    @Test
    void activateLease_success() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.D);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));

        Map<String, Object> res = service.activateLease(1L);

        assertEquals("Lease activated successfully", res.get("message"));
        assertEquals(LeaseAgreement.LeaseStatus.A, lease.getStatus());
        verify(repo).save(lease);
    }

    // 27
    @Test
    void activateLease_notDraft_throws() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.A);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));
        assertThrows(InvalidStatusTransitionException.class,
            () -> service.activateLease(1L));
    }

    // 28
    @Test
    void renewLease_success() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.A);
        lease.setMonthlyRent(20000.0);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));

        Map<String, Object> body = new HashMap<>();
        body.put("escalationPercent", 10.0);

        Map<String, Object> res = service.renewLease(1L, body);

        assertEquals("Lease renewed successfully", res.get("message"));
        assertEquals(LeaseAgreement.LeaseStatus.R, lease.getStatus());
        verify(repo).save(lease);
    }
}
