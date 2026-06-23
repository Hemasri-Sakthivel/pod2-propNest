package com.cog.propNest.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.cog.propNest.common.response.ErrorResponse;
import com.cog.propNest.module.tenantOnboardingLease.exception.DocumentStorageException;
import com.cog.propNest.module.tenantOnboardingLease.exception.FileSizeLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidDocumentException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.StorageCapacityExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.TenantApplicationNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.UploadLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for {@link GlobalExceptionHandler} verifying each exception type
 * is mapped to the correct HTTP status and a populated {@link ErrorResponse}.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/propNest/tenantOnboarding/test");
    }

    @Test
    void kycNotFound_maps404() {
        var res = handler.handleModuleNotFound(new KycNotFoundException("nf"), request);
        assertEquals(404, res.getStatusCode().value());
        assertEquals("nf", res.getBody().getMessage());
    }

    @Test
    void applicationNotFound_maps404() {
        var res = handler.handleModuleNotFound(
            new TenantApplicationNotFoundException("nf"), request);
        assertEquals(404, res.getStatusCode().value());
    }

    @Test
    void leaseNotFound_maps404() {
        var res = handler.handleModuleNotFound(
            new LeaseAgreementNotFoundException("nf"), request);
        assertEquals(404, res.getStatusCode().value());
    }

    @Test
    void invalidRequest_maps400() {
        var res = handler.handleBadRequest(new InvalidRequestException("bad"), request);
        assertEquals(400, res.getStatusCode().value());
        assertEquals("bad", res.getBody().getMessage());
    }

    @Test
    void invalidDocument_maps400() {
        var res = handler.handleBadRequest(new InvalidDocumentException("bad doc"), request);
        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void fileTooLarge_maps413() {
        var res = handler.handleFileTooLarge(
            new FileSizeLimitExceededException("too big"), request);
        assertEquals(413, res.getStatusCode().value());
    }

    @Test
    void uploadLimit_maps409() {
        var res = handler.handleModuleConflict(
            new UploadLimitExceededException("limit"), request);
        assertEquals(409, res.getStatusCode().value());
    }

    @Test
    void invalidStatusTransition_maps409() {
        var res = handler.handleModuleConflict(
            new InvalidStatusTransitionException("transition"), request);
        assertEquals(409, res.getStatusCode().value());
    }

    @Test
    void storageCapacity_maps507() {
        var res = handler.handleStorageFull(
            new StorageCapacityExceededException("full"), request);
        assertEquals(507, res.getStatusCode().value());
    }

    @Test
    void storageFailure_maps500() {
        var res = handler.handleStorageFailure(
            new DocumentStorageException("io"), request);
        assertEquals(500, res.getStatusCode().value());
    }

    @Test
    void maxUploadSize_maps413() {
        var res = handler.handleUploadTooLarge(
            new MaxUploadSizeExceededException(5L), request);
        assertEquals(413, res.getStatusCode().value());
    }

    @Test
    void genericException_maps500() {
        var res = handler.handleGeneric(new RuntimeException("boom"), request);
        assertEquals(500, res.getStatusCode().value());
    }
}
