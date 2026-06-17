

package com.cog.propNest.module.tenantOnboardingLease.repository;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TenantApplicationRepository
    extends JpaRepository<TenantApplication, Long> {

    List<TenantApplication> findByStatus(
        TenantApplication.ApplicationStatus status);
}