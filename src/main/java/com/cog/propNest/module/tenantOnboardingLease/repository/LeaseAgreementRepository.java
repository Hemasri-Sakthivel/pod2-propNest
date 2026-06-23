

package com.cog.propNest.module.tenantOnboardingLease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;

@Repository
public interface LeaseAgreementRepository
    extends JpaRepository<LeaseAgreement, Long> {

    List<LeaseAgreement> findByStatus(
        LeaseAgreement.LeaseStatus status);
}