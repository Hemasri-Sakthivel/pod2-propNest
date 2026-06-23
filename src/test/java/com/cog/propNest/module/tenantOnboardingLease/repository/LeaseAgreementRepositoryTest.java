package com.cog.propNest.module.tenantOnboardingLease.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;

/**
 * Repository-layer tests for {@link LeaseAgreementRepository}
 * ({@code @SpringBootTest} against the real datasource; transactional rollback).
 */
@SpringBootTest
@Transactional
class LeaseAgreementRepositoryTest {

    @Autowired
    private LeaseAgreementRepository repo;

    @Test
    void repositoryIsWired() {
        assertNotNull(repo);
    }

    @Test
    void findByStatus_draft_executes() {
        assertNotNull(repo.findByStatus(LeaseAgreement.LeaseStatus.D));
    }

    @Test
    void findByStatus_active_executes() {
        assertNotNull(repo.findByStatus(LeaseAgreement.LeaseStatus.A));
    }

    @Test
    void findAll_returnsList() {
        assertNotNull(repo.findAll());
    }

    @Test
    void count_isNonNegative() {
        assertTrue(repo.count() >= 0);
    }
}
