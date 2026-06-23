package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.InvoiceStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
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
 * Repository-layer tests for {@link RentInvoiceRepository}. Runs against the
 * configured MySQL ({@code replace = NONE}); each test is wrapped in a
 * transaction that is rolled back, so the seeded data is left untouched.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentInvoiceRepositoryTest {

    @Autowired
    private RentInvoiceRepository repository;

    private RentInvoice sample() {
        RentInvoice inv = new RentInvoice();
        inv.setLeaseId(1L);
        inv.setTenantId(1L);
        inv.setBillingMonth("2025-09");
        inv.setRentAmount(new BigDecimal("18000.00"));
        inv.setOtherCharges(new BigDecimal("500.00"));
        inv.setTotalAmount(new BigDecimal("18500.00"));
        inv.setDueDate(LocalDate.parse("2025-09-05"));
        inv.setStatus(InvoiceStatus.Generated);
        inv.setPaymentMethod("Online");
        return inv;
    }

    @Test
    void save_assignsGeneratedId() {
        RentInvoice saved = repository.save(sample());
        assertNotNull(saved.getInvoiceId());
    }

    @Test
    void save_populatesCreatedAt() {
        RentInvoice saved = repository.saveAndFlush(sample());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findById_returnsSavedEntity() {
        Long id = repository.save(sample()).getInvoiceId();
        Optional<RentInvoice> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("2025-09", found.get().getBillingMonth());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(repository.findById(99999999L).isEmpty());
    }

    @Test
    void findAll_includesSavedEntity() {
        Long id = repository.save(sample()).getInvoiceId();
        assertTrue(repository.findAll().stream().anyMatch(i -> i.getInvoiceId().equals(id)));
    }

    @Test
    void existsById_trueAfterSave() {
        Long id = repository.save(sample()).getInvoiceId();
        assertTrue(repository.existsById(id));
    }

    @Test
    void deleteById_removesEntity() {
        Long id = repository.save(sample()).getInvoiceId();
        repository.deleteById(id);
        assertFalse(repository.existsById(id));
    }

    @Test
    void update_persistsChangedStatus() {
        RentInvoice saved = repository.saveAndFlush(sample());
        saved.setStatus(InvoiceStatus.Paid);
        repository.saveAndFlush(saved);
        assertEquals(InvoiceStatus.Paid, repository.findById(saved.getInvoiceId()).orElseThrow().getStatus());
    }
}
