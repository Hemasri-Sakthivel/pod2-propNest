package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantKycDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;

/**
 * Unit tests for {@link TenantKycService} — covers createKyc, getAllKyc,
 * getKycById and verifyKyc, including the custom exceptions each can raise.
 */
@ExtendWith(MockitoExtension.class)
class TenantKycServiceTest {

    @Mock
    private TenantKycRepository repo;

    @InjectMocks
    private TenantKycService service;

    private TenantKycDTO validDto() {
        TenantKycDTO dto = new TenantKycDTO();
        dto.setTenantId(1L);
        dto.setDocumentType("Passport");
        dto.setDocumentRef("tenant_1/doc.pdf");
        return dto;
    }

    // 1
    @Test
    void createKyc_success() {
        Map<String, Object> res = service.createKyc(validDto());
        assertEquals("KYC document uploaded successfully", res.get("message"));
        verify(repo).save(any(TenantKyc.class));
    }

    // 2
    @Test
    void createKyc_nullTenantId_throwsInvalidRequest() {
        TenantKycDTO dto = validDto();
        dto.setTenantId(null);
        assertThrows(InvalidRequestException.class, () -> service.createKyc(dto));
        verify(repo, never()).save(any());
    }

    // 3
    @Test
    void createKyc_invalidDocumentType_throwsInvalidRequest() {
        TenantKycDTO dto = validDto();
        dto.setDocumentType("DrivingLicense");
        assertThrows(InvalidRequestException.class, () -> service.createKyc(dto));
    }

    // 4
    @Test
    void createKyc_missingDocumentRef_throwsInvalidRequest() {
        TenantKycDTO dto = validDto();
        dto.setDocumentRef("");
        assertThrows(InvalidRequestException.class, () -> service.createKyc(dto));
    }

    // 5
    @Test
    void getAllKyc_returnsRecords() {
        when(repo.findAll()).thenReturn(List.of(new TenantKyc()));
        Map<String, Object> res = service.getAllKyc(null);
        assertEquals(1, res.get("totalElements"));
    }

    // 6
    @Test
    void getAllKyc_empty_throwsKycNotFound() {
        when(repo.findAll()).thenReturn(List.of());
        assertThrows(KycNotFoundException.class, () -> service.getAllKyc(null));
    }

    // 7
    @Test
    void getKycById_found() {
        TenantKyc kyc = new TenantKyc();
        kyc.setKycId(5L);
        when(repo.findById(5L)).thenReturn(Optional.of(kyc));
        Map<String, Object> res = service.getKycById(5L);
        assertSame(kyc, res.get("data"));
    }

    // 8
    @Test
    void getKycById_notFound_throwsKycNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(KycNotFoundException.class, () -> service.getKycById(99L));
    }

    // 9
    @Test
    void verifyKyc_verified_setsCerifiedDate() {
        TenantKyc kyc = new TenantKyc();
        kyc.setStatus(TenantKyc.KycStatus.P);
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));

        Map<String, String> body = new HashMap<>();
        body.put("status", "V");

        Map<String, Object> res = service.verifyKyc(1L, body);

        assertEquals("KYC verified successfully", res.get("message"));
        assertEquals(TenantKyc.KycStatus.V, kyc.getStatus());
        assertNotNull(kyc.getCerifiedDate());
        verify(repo).save(kyc);
    }

    // 10
    @Test
    void verifyKyc_notPending_throwsInvalidStatusTransition() {
        TenantKyc kyc = new TenantKyc();
        kyc.setStatus(TenantKyc.KycStatus.V);
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));

        Map<String, String> body = new HashMap<>();
        body.put("status", "V");

        assertThrows(InvalidStatusTransitionException.class,
            () -> service.verifyKyc(1L, body));
    }
}
