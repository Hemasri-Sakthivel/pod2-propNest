package com.cog.propNest.module.tenantOnboardingLease.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantApplication;

/**
 * Repository-layer tests for {@link TenantApplicationRepository}
 * ({@code @SpringBootTest} against the real datasource; transactional rollback).
 */
@SpringBootTest
@Transactional
class TenantApplicationRepositoryTest {

    @Autowired
    private TenantApplicationRepository repo;

    @Test
    void repositoryIsWired() {
        assertNotNull(repo);
    }

    @Test
    void findByStatus_submitted_executes() {
        assertNotNull(repo.findByStatus(TenantApplication.ApplicationStatus.S));
    }

    @Test
    void findByStatus_approved_executes() {
        assertNotNull(repo.findByStatus(TenantApplication.ApplicationStatus.A));
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
