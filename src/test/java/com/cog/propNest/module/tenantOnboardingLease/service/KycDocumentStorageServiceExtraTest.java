package com.cog.propNest.module.tenantOnboardingLease.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Additional (filesystem-free) coverage for {@link KycDocumentStorageService}.
 */
class KycDocumentStorageServiceExtraTest {

    private final KycDocumentStorageService storage = new KycDocumentStorageService();

    @Test
    void buildRelativeRef_hasExpectedFormat() {
        String ref = storage.buildRelativeRef(3L, "KYC-3-abcd1234");
        assertEquals("tenant_3/KYC-3-abcd1234.pdf", ref);
    }

    @Test
    void generateDocumentId_isUniqueAcrossCalls() {
        String a = storage.generateDocumentId(1L);
        String b = storage.generateDocumentId(1L);
        assertNotEquals(a, b);
    }

    @Test
    void generateDocumentId_embedsDifferentTenantIds() {
        assertTrue(storage.generateDocumentId(2L).startsWith("KYC-2-"));
        assertTrue(storage.generateDocumentId(99L).startsWith("KYC-99-"));
    }
}
