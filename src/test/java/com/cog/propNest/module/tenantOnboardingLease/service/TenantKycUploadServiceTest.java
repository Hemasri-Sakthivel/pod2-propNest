package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
import com.cog.propNest.module.tenantOnboardingLease.exception.FileSizeLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidDocumentException;
import com.cog.propNest.module.tenantOnboardingLease.exception.UploadLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;

/**
 * Unit tests for the document upload / availability flow of {@link TenantKycService}.
 * The configurable limits (normally injected via {@code @Value}) are set with
 * {@link ReflectionTestUtils} because this is a plain Mockito unit test.
 */
@ExtendWith(MockitoExtension.class)
class TenantKycUploadServiceTest {

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

    /** A mock MultipartFile that looks like a genuine PDF of the given size. */
    private MultipartFile pdfFile(long size) throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("passport.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream())
            .thenReturn(new ByteArrayInputStream("%PDF-1.4 sample".getBytes()));
        when(file.getSize()).thenReturn(size);
        return file;
    }

    // 11
    @Test
    void uploadDocument_success() throws Exception {
        MultipartFile file = pdfFile(1_000L);
        when(repo.countByTenantId(1L)).thenReturn(0L);
        when(repo.sumTotalFileSize()).thenReturn(0L);
        when(storage.generateDocumentId(1L)).thenReturn("KYC-1-abcd1234");
        when(storage.store(eq(1L), eq("KYC-1-abcd1234"), any()))
            .thenReturn("tenant_1/KYC-1-abcd1234.pdf");

        Map<String, Object> res = service.uploadDocument(1L, "Passport", file);

        assertEquals("KYC document uploaded successfully", res.get("message"));
        assertEquals("KYC-1-abcd1234", res.get("documentId"));
        assertEquals("tenant_1/KYC-1-abcd1234.pdf", res.get("documentRef"));
        verify(repo).save(any(TenantKyc.class));
    }

    // 12
    @Test
    void uploadDocument_noFile_throwsInvalidDocument() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(InvalidDocumentException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    // 13
    @Test
    void uploadDocument_notPdf_throwsInvalidDocument() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("passport.png");
        assertThrows(InvalidDocumentException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    // 14
    @Test
    void uploadDocument_tooLarge_throwsFileSizeLimit() throws Exception {
        ReflectionTestUtils.setField(service, "maxFileSizeBytes", 100L);
        MultipartFile file = pdfFile(5_000L);
        assertThrows(FileSizeLimitExceededException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    // 15
    @Test
    void uploadDocument_limitReached_throwsUploadLimit() throws Exception {
        MultipartFile file = pdfFile(1_000L);
        when(repo.countByTenantId(1L)).thenReturn(10L);
        assertThrows(UploadLimitExceededException.class,
            () -> service.uploadDocument(1L, "Passport", file));
    }

    // 16
    @Test
    void verifyDocument_missingFile_returnsAvailableFalse() {
        TenantKyc kyc = new TenantKyc();
        kyc.setDocumentRef("tenant_1/missing.pdf");
        when(repo.findById(1L)).thenReturn(Optional.of(kyc));
        when(storage.exists("tenant_1/missing.pdf")).thenReturn(false);

        Map<String, Object> res = service.verifyDocument(1L);

        assertEquals(false, res.get("available"));
    }
}
