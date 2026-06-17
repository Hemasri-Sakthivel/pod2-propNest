package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.DepositStatus;
import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository-layer tests for {@link SecurityDepositLedgerRepository}, against
 * MySQL with per-test transaction rollback.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityDepositLedgerRepositoryTest {

    @Autowired
    private SecurityDepositLedgerRepository repository;

    private SecurityDepositLedger sample() {
        SecurityDepositLedger d = new SecurityDepositLedger();
        d.setLeaseId(1L);
        d.setTenantId(1L);
        d.setDepositAmount(new BigDecimal("54000.00"));
        d.setReceivedDate(LocalDate.parse("2025-01-01"));
        d.setRefundAmount(BigDecimal.ZERO);
        d.setStatus(DepositStatus.Held);
        d.setRemarks("test deposit");
        return d;
    }

    @Test
    void save_assignsGeneratedId() {
        assertNotNull(repository.save(sample()).getDepositId());
    }

    @Test
    void save_populatesCreatedAt() {
        assertNotNull(repository.saveAndFlush(sample()).getCreatedAt());
    }

    @Test
    void findById_returnsSavedEntity() {
        Long id = repository.save(sample()).getDepositId();
        Optional<SecurityDepositLedger> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals(DepositStatus.Held, found.get().getStatus());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(repository.findById(99999999L).isEmpty());
    }

    @Test
    void save_persistsDeductionsJson() {
        SecurityDepositLedger d = sample();
        d.setDeductionsJSON("{\"DamageRepair\": 2000}");
        Long id = repository.saveAndFlush(d).getDepositId();
        assertEquals("{\"DamageRepair\": 2000}",
                repository.findById(id).orElseThrow().getDeductionsJSON());
    }

    @Test
    void existsById_trueAfterSave() {
        Long id = repository.save(sample()).getDepositId();
        assertTrue(repository.existsById(id));
    }

    @Test
    void deleteById_removesEntity() {
        Long id = repository.save(sample()).getDepositId();
        repository.deleteById(id);
        assertFalse(repository.existsById(id));
    }

    @Test
    void update_persistsFullRefund() {
        SecurityDepositLedger saved = repository.saveAndFlush(sample());
        saved.setStatus(DepositStatus.FullyRefunded);
        saved.setRefundAmount(new BigDecimal("54000.00"));
        saved.setRefundDate(LocalDate.parse("2025-12-31"));
        repository.saveAndFlush(saved);
        assertEquals(DepositStatus.FullyRefunded,
                repository.findById(saved.getDepositId()).orElseThrow().getStatus());
    }
}
