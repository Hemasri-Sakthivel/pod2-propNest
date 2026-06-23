package com.cog.propNest.module.rentDepositManagement.repository;

import com.cog.propNest.module.rentDepositManagement.entity.PaymentMethod;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
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
 * Repository-layer tests for {@link RentPaymentRepository}, against MySQL with
 * per-test transaction rollback.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentPaymentRepositoryTest {

    @Autowired
    private RentPaymentRepository repository;

    private RentPayment sample(PaymentStatus status) {
        RentPayment p = new RentPayment();
        p.setInvoiceId(1L);
        p.setPaidAmount(new BigDecimal("18500.00"));
        p.setPaymentDate(LocalDate.parse("2025-05-03"));
        p.setMethod(PaymentMethod.Online);
        p.setStatus(status);
        p.setRemarks("test payment");
        return p;
    }

    @Test
    void save_assignsGeneratedId() {
        assertNotNull(repository.save(sample(PaymentStatus.Received)).getPaymentId());
    }

    @Test
    void save_populatesCreatedAt() {
        assertNotNull(repository.saveAndFlush(sample(PaymentStatus.Received)).getCreatedAt());
    }

    @Test
    void findById_returnsSavedEntity() {
        Long id = repository.save(sample(PaymentStatus.Partial)).getPaymentId();
        Optional<RentPayment> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals(PaymentStatus.Partial, found.get().getStatus());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(repository.findById(99999999L).isEmpty());
    }

    @Test
    void findAll_includesSavedEntity() {
        Long id = repository.save(sample(PaymentStatus.Failed)).getPaymentId();
        assertTrue(repository.findAll().stream().anyMatch(p -> p.getPaymentId().equals(id)));
    }

    @Test
    void existsById_trueAfterSave() {
        Long id = repository.save(sample(PaymentStatus.Received)).getPaymentId();
        assertTrue(repository.existsById(id));
    }

    @Test
    void deleteById_removesEntity() {
        Long id = repository.save(sample(PaymentStatus.Received)).getPaymentId();
        repository.deleteById(id);
        assertFalse(repository.existsById(id));
    }

    @Test
    void update_persistsChangedMethod() {
        RentPayment saved = repository.saveAndFlush(sample(PaymentStatus.Partial));
        saved.setMethod(PaymentMethod.BankTransfer);
        repository.saveAndFlush(saved);
        assertEquals(PaymentMethod.BankTransfer,
                repository.findById(saved.getPaymentId()).orElseThrow().getMethod());
    }
}
