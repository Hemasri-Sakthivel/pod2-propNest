package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link SecurityDepositLedger} records.
 */
public interface SecurityDepositLedgerRepository extends JpaRepository<SecurityDepositLedger, Long> {
}
