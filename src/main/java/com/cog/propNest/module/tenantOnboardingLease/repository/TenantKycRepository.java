

package com.cog.propNest.module.tenantOnboardingLease.repository;

import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantKycRepository
    extends JpaRepository<TenantKyc, Long> {

    List<TenantKyc> findByStatus(
        TenantKyc.KycStatus status);

    // How many documents this tenant has already uploaded — used to enforce
    // the per-tenant upload count limit.
    long countByTenantId(Long tenantId);

    List<TenantKyc> findByTenantId(Long tenantId);

    Optional<TenantKyc> findByDocumentId(String documentId);

    // Total bytes occupied by all stored documents — used to enforce the
    // portal's overall storage capacity.
    @Query("SELECT COALESCE(SUM(k.fileSize), 0) FROM TenantKyc k")
    long sumTotalFileSize();
}