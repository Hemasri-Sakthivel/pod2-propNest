package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;

/**
 * Additional edge-case coverage for {@link TenantKycService}.
 */
@ExtendWith(MockitoExtension.class)
class TenantKycServiceExtraTest {

    @Mock
    private TenantKycRepository repo;

    @InjectMocks
    private TenantKycService service;

    @Test
    void createKyc_nullDocumentType_throwsInvalidRequest() {
        TenantKycDTO dto = new TenantKycDTO();
        dto.setTenantId(1L);
        dto.setDocumentRef("tenant_1/doc.pdf");
        assertThrows(InvalidRequestException.class, () -> service.createKyc(dto));
    }

    @Test
    void createKyc_emptyDocumentType_throwsInvalidRequest() {
        TenantKycDTO dto = new TenantKycDTO();
        dto.setTenantId(1L);
        dto.setDocumentType("");
        dto.setDocumentRef("tenant_1/doc.pdf");
        assertThrows(InvalidRequestException.class, () -> service.createKyc(dto));
    }

    @Test
    void getAllKyc_withValidStatusFilter_returnsRecords() {
        when(repo.findByStatus(TenantKyc.KycStatus.P))
            .thenReturn(List.of(new TenantKyc(), new TenantKyc()));
        Map<String, Object> res = service.getAllKyc("P");
        assertEquals(2, res.get("totalElements"));
    }

    @Test
    void getAllKyc_withInvalidStatusFilter_throwsInvalidRequest() {
        assertThrows(InvalidRequestException.class, () -> service.getAllKyc("X"));
    }

    @Test
    void verifyKyc_rejected_doesNotSetCerifiedDate() {
        TenantKyc kyc = new TenantKyc();
        kyc.setStatus(TenantKyc.KycStatus.P);
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));

        Map<String, String> body = new HashMap<>();
        body.put("status", "R");

        service.verifyKyc(1L, body);

        assertEquals(TenantKyc.KycStatus.R, kyc.getStatus());
        assertNull(kyc.getCerifiedDate());
    }

    @Test
    void verifyKyc_invalidStatusValue_throwsInvalidRequest() {
        TenantKyc kyc = new TenantKyc();
        kyc.setStatus(TenantKyc.KycStatus.P);
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));

        Map<String, String> body = new HashMap<>();
        body.put("status", "X");

        assertThrows(InvalidRequestException.class, () -> service.verifyKyc(1L, body));
    }

    @Test
    void verifyKyc_notFound_throwsKycNotFound() {
        when(repo.findById(50L)).thenReturn(Optional.empty());
        Map<String, String> body = new HashMap<>();
        body.put("status", "V");
        assertThrows(KycNotFoundException.class, () -> service.verifyKyc(50L, body));
    }
}
