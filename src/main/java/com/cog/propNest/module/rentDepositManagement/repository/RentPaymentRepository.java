package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link RentPayment} records.
 */
public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {
}
