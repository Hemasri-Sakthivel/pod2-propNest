package com.cog.propNest.module.tenantOnboardingLease.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;

/**
 * Repository-layer tests for {@link TenantKycRepository}.
 *
 * <p>Uses {@code @SpringBootTest} so the real JPA repositories run against the
 * configured datasource (MySQL). {@code @Transactional} rolls back each test, so
 * nothing is persisted. The assertions are read-only and seed-independent: they
 * confirm every custom query maps to valid SQL and executes against the schema.
 */
@SpringBootTest
@Transactional
class TenantKycRepositoryTest {

    @Autowired
    private TenantKycRepository repo;

    @Test
    void repositoryIsWired() {
        assertNotNull(repo);
    }

    @Test
    void findByStatus_executesAndReturnsList() {
        assertNotNull(repo.findByStatus(TenantKyc.KycStatus.P));
    }

    @Test
    void count_isNonNegative() {
        assertTrue(repo.count() >= 0);
    }

    @Test
    void countByTenantId_unknownTenant_isZero() {
        assertEquals(0, repo.countByTenantId(-1L));
    }

    @Test
    void findByTenantId_unknownTenant_isEmpty() {
        assertTrue(repo.findByTenantId(-1L).isEmpty());
    }

    @Test
    void findByDocumentId_unknownId_isEmpty() {
        assertTrue(repo.findByDocumentId("__does_not_exist__").isEmpty());
    }

    @Test
    void sumTotalFileSize_isNonNegative() {
        assertTrue(repo.sumTotalFileSize() >= 0);
    }
}
