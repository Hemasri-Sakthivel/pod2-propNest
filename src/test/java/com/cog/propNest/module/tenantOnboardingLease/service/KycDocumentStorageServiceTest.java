package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KycDocumentStorageService} that do not touch the
 * filesystem (id generation and the blank-reference guard).
 */
class KycDocumentStorageServiceTest {

    private final KycDocumentStorageService storage = new KycDocumentStorageService();

    // 29
    @Test
    void generateDocumentId_hasTenantPrefix() {
        String documentId = storage.generateDocumentId(7L);
        assertTrue(documentId.startsWith("KYC-7-"),
            "documentId should embed the tenantId, was: " + documentId);
    }

    // 30
    @Test
    void exists_blankOrNullRef_returnsFalse() {
        assertFalse(storage.exists(""));
        assertFalse(storage.exists(null));
    }
}
