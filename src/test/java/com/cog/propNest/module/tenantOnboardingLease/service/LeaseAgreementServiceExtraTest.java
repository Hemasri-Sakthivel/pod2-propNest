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

import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.LeaseAgreementRepository;

/**
 * Additional edge-case coverage for {@link LeaseAgreementService}.
 */
@ExtendWith(MockitoExtension.class)
class LeaseAgreementServiceExtraTest {

    @Mock
    private LeaseAgreementRepository repo;

    @InjectMocks
    private LeaseAgreementService service;

    @Test
    void getAllLeases_returnsRecords() {
        when(repo.findAll()).thenReturn(List.of(new LeaseAgreement()));
        Map<String, Object> res = service.getAllLeases(null);
        assertEquals(1, res.get("totalElements"));
    }

    @Test
    void getLeaseById_found() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setLeaseId(8L);
        when(repo.findById(8L)).thenReturn(Optional.of(lease));
        Map<String, Object> res = service.getLeaseById(8L);
        assertSame(lease, res.get("data"));
    }

    @Test
    void activateLease_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> service.activateLease(99L));
    }

    @Test
    void renewLease_notActive_throws() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.D);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));
        Map<String, Object> body = new HashMap<>();
        body.put("escalationPercent", 5.0);
        assertThrows(InvalidStatusTransitionException.class,
            () -> service.renewLease(1L, body));
    }

    @Test
    void renewLease_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        Map<String, Object> body = new HashMap<>();
        body.put("escalationPercent", 5.0);
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> service.renewLease(99L, body));
    }

    @Test
    void terminateLease_success() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.A);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));

        Map<String, String> body = new HashMap<>();
        Map<String, Object> res = service.terminateLease(1L, body);

        assertEquals("Lease terminated successfully", res.get("message"));
        assertEquals(LeaseAgreement.LeaseStatus.T, lease.getStatus());
        verify(repo).save(lease);
    }

    @Test
    void terminateLease_notActive_throws() {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setStatus(LeaseAgreement.LeaseStatus.D);
        when(repo.findById(1L)).thenReturn(Optional.of(lease));
        assertThrows(InvalidStatusTransitionException.class,
            () -> service.terminateLease(1L, new HashMap<>()));
    }

    @Test
    void terminateLease_notFound_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaseAgreementNotFoundException.class,
            () -> service.terminateLease(99L, new HashMap<>()));
    }
}
