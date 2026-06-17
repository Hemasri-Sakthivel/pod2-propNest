package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
import com.cog.propNest.module.tenantOnboardingLease.exception.DocumentStorageException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.StorageCapacityExceededException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;

/**
 * Additional coverage for the document upload / verify flow of {@link TenantKycService}.
 */
@ExtendWith(MockitoExtension.class)
class TenantKycUploadServiceExtraTest {

    @Mock
    private TenantKycRepository repo;

    @Mock
    private KycDocumentStorageService storage;

    @InjectMocks
    private TenantKycService service;

    @BeforeEach
    void setLimits() {
        ReflectionTestUtils.setField(service, "maxFileSizeBytes", 5_000_000L);
        ReflectionTestUtils.setField(service, "maxDocumentsPerTenant", 10L);
        ReflectionTestUtils.setField(service, "maxTotalCapacityBytes", 500_000_000L);
    }

    private MultipartFile pdfFile(long size) throws Exception {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("passport.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream())
            .thenReturn(new ByteArrayInputStream("%PDF-1.4 sample".getBytes()));
        when(file.getSize()).thenReturn(size);
        return file;
    }

    @Test
    void uploadDocument_capacityExceeded_throwsStorageCapacity() throws Exception {
        MultipartFile file = pdfFile(1_000L);
        when(repo.countByTenantId(1L)).thenReturn(0L);
        when(repo.sumTotalFileSize()).thenReturn(500_000_000L);
        assertThrows(StorageCapacityExceededException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    @Test
    void uploadDocument_storeFailure_throwsDocumentStorage() throws Exception {
        MultipartFile file = pdfFile(1_000L);
        when(repo.countByTenantId(1L)).thenReturn(0L);
        when(repo.sumTotalFileSize()).thenReturn(0L);
        when(storage.generateDocumentId(1L)).thenReturn("KYC-1-zzzz");
        when(storage.store(eq(1L), eq("KYC-1-zzzz"), any()))
            .thenThrow(new RuntimeException("disk error"));
        assertThrows(DocumentStorageException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    @Test
    void uploadDocument_nullTenantId_throwsInvalidRequest() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        assertThrows(InvalidRequestException.class,
            () -> service.uploadDocument(null, "Passport", file));
    }

    @Test
    void uploadDocument_invalidDocumentType_throwsInvalidRequest() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        assertThrows(InvalidRequestException.class,
            () -> service.uploadDocument(1L, "DrivingLicense", file));
    }

    @Test
    void verifyDocument_available_returnsTrue() {
        TenantKyc kyc = new TenantKyc();
        kyc.setDocumentId("KYC-1-abcd");
        kyc.setDocumentRef("tenant_1/KYC-1-abcd.pdf");
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));
        when(storage.exists("tenant_1/KYC-1-abcd.pdf")).thenReturn(true);

        Map<String, Object> res = service.verifyDocument(1L);

        assertEquals(true, res.get("available"));
        assertEquals("KYC-1-abcd", res.get("documentId"));
    }

    @Test
    void verifyDocument_notFound_throwsKycNotFound() {
        when(repo.findById(77L)).thenReturn(Optional.empty());
        assertThrows(KycNotFoundException.class, () -> service.verifyDocument(77L));
    }
}
