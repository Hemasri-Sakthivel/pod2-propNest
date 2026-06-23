package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link RentInvoice} records.
 */
public interface RentInvoiceRepository extends JpaRepository<RentInvoice, Long> {
}
