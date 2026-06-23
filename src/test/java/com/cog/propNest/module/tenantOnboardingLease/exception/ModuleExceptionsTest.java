package com.cog.propNest.module.tenantOnboardingLease.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Verifies each module exception carries its message and is an unchecked
 * (RuntimeException) so it flows to the global handler without {@code throws}.
 */
class ModuleExceptionsTest {

    @Test
    void kycNotFound_carriesMessage() {
        KycNotFoundException ex = new KycNotFoundException("kyc");
        assertEquals("kyc", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void tenantApplicationNotFound_carriesMessage() {
        var ex = new TenantApplicationNotFoundException("app");
        assertEquals("app", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void leaseAgreementNotFound_carriesMessage() {
        var ex = new LeaseAgreementNotFoundException("lease");
        assertEquals("lease", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidRequest_carriesMessage() {
        var ex = new InvalidRequestException("req");
        assertEquals("req", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidDocument_carriesMessage() {
        var ex = new InvalidDocumentException("doc");
        assertEquals("doc", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void fileSizeLimit_carriesMessage() {
        var ex = new FileSizeLimitExceededException("size");
        assertEquals("size", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void uploadLimit_carriesMessage() {
        var ex = new UploadLimitExceededException("count");
        assertEquals("count", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void storageCapacity_carriesMessage() {
        var ex = new StorageCapacityExceededException("capacity");
        assertEquals("capacity", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void documentStorage_carriesMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        var ex = new DocumentStorageException("store", cause);
        assertEquals("store", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void invalidStatusTransition_carriesMessage() {
        var ex = new InvalidStatusTransitionException("transition");
        assertEquals("transition", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
