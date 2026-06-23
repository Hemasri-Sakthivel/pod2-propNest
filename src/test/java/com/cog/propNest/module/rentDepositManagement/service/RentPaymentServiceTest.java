package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.dto.PaymentResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentMethod;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentAlreadyReceivedException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentNotFoundException;
import com.cog.propNest.module.rentDepositManagement.repository.RentInvoiceRepository;
import com.cog.propNest.module.rentDepositManagement.repository.RentPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Service-layer unit tests for {@link RentPaymentService} (Mockito). */
class RentPaymentServiceTest {

    private RentPaymentRepository paymentRepository;
    private RentInvoiceRepository invoiceRepository;
    private RentPaymentService service;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(RentPaymentRepository.class);
        invoiceRepository = mock(RentInvoiceRepository.class);
        service = new RentPaymentService(paymentRepository, invoiceRepository);
        when(paymentRepository.save(any(RentPayment.class))).thenAnswer(i -> i.getArgument(0));
        when(invoiceRepository.existsById(anyLong())).thenReturn(true);
    }

    private CreatePaymentRequest validCreate() {
        CreatePaymentRequest r = new CreatePaymentRequest();
        r.setInvoiceId(1L);
        r.setPaidAmount(new BigDecimal("18500.00"));
        r.setPaymentDate(LocalDate.parse("2025-05-03"));
        r.setMethod("Online");
        r.setStatus("Received");
        r.setRemarks("Full payment");
        return r;
    }

    private RentPayment existing(long id, PaymentStatus status) {
        RentPayment p = new RentPayment();
        p.setPaymentId(id);
        p.setInvoiceId(1L);
        p.setPaidAmount(new BigDecimal("9000.00"));
        p.setPaymentDate(LocalDate.parse("2025-06-07"));
        p.setMethod(PaymentMethod.Cash);
        p.setStatus(status);
        return p;
    }

    @Test
    void createPayment_success_returnsResponse() {
        PaymentResponse resp = service.createPayment(validCreate());
        assertNotNull(resp);
        assertEquals("Online", resp.getMethod());
    }

    @Test
    void createPayment_savesEntity() {
        service.createPayment(validCreate());
        verify(paymentRepository).save(any(RentPayment.class));
    }

    @Test
    void createPayment_missingInvoiceId_throws() {
        CreatePaymentRequest r = validCreate();
        r.setInvoiceId(null);
        assertEquals("invoiceId is required",
                assertThrows(BadRequestException.class, () -> service.createPayment(r)).getMessage());
    }

    @Test
    void createPayment_missingPaidAmount_throws() {
        CreatePaymentRequest r = validCreate();
        r.setPaidAmount(null);
        assertEquals("paidAmount is required",
                assertThrows(BadRequestException.class, () -> service.createPayment(r)).getMessage());
    }

    @Test
    void createPayment_invoiceNotFound_throws() {
        when(invoiceRepository.existsById(1L)).thenReturn(false);
        assertThrows(InvoiceNotFoundException.class, () -> service.createPayment(validCreate()));
    }

    @Test
    void createPayment_invalidMethod_throws() {
        CreatePaymentRequest r = validCreate();
        r.setMethod("Crypto");
        assertEquals("Invalid payment method: Crypto",
                assertThrows(BadRequestException.class, () -> service.createPayment(r)).getMessage());
    }

    @Test
    void createPayment_invalidStatus_throws() {
        CreatePaymentRequest r = validCreate();
        r.setStatus("Bounced");
        assertEquals("Invalid payment status: Bounced",
                assertThrows(BadRequestException.class, () -> service.createPayment(r)).getMessage());
    }

    @Test
    void getAllPayments_returnsMappedList() {
        when(paymentRepository.findAll()).thenReturn(List.of(existing(1, PaymentStatus.Partial)));
        assertEquals(1, service.getAllPayments().size());
    }

    @Test
    void getPaymentById_found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing(1, PaymentStatus.Partial)));
        assertEquals(1L, service.getPaymentById(1L).getPaymentId());
    }

    @Test
    void getPaymentById_notFound_throws() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> service.getPaymentById(99L));
    }

    @Test
    void updatePayment_partialToReceived_success() {
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(existing(2, PaymentStatus.Partial)));
        UpdatePaymentRequest r = new UpdatePaymentRequest();
        r.setStatus("Received");
        assertEquals("Received", service.updatePayment(2L, r).getStatus());
    }

    @Test
    void updatePayment_alreadyReceived_throws() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing(1, PaymentStatus.Received)));
        assertEquals("Cannot modify a Received payment",
                assertThrows(PaymentAlreadyReceivedException.class,
                        () -> service.updatePayment(1L, new UpdatePaymentRequest())).getMessage());
    }

    @Test
    void updatePayment_notFound_throws() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class,
                () -> service.updatePayment(99L, new UpdatePaymentRequest()));
    }

    @Test
    void updatePayment_updatesMethod() {
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(existing(2, PaymentStatus.Partial)));
        UpdatePaymentRequest r = new UpdatePaymentRequest();
        r.setMethod("BankTransfer");
        assertEquals("BankTransfer", service.updatePayment(2L, r).getMethod());
    }
}
