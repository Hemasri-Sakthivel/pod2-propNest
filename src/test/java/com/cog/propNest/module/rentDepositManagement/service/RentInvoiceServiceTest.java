package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.dto.InvoiceResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.entity.InvoiceStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.repository.RentInvoiceRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Service-layer unit tests for {@link RentInvoiceService} (Mockito). */
class RentInvoiceServiceTest {

    private RentInvoiceRepository invoiceRepository;
    private RentInvoiceService service;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(RentInvoiceRepository.class);
        service = new RentInvoiceService(invoiceRepository);
        when(invoiceRepository.save(any(RentInvoice.class))).thenAnswer(i -> i.getArgument(0));
    }

    private CreateInvoiceRequest validCreate() {
        CreateInvoiceRequest r = new CreateInvoiceRequest();
        r.setLeaseId(1L);
        r.setTenantId(1L);
        r.setBillingMonth("2025-05");
        r.setRentAmount(new BigDecimal("18000.00"));
        r.setOtherCharges(new BigDecimal("500.00"));
        r.setTotalAmount(new BigDecimal("18500.00"));
        r.setDueDate(LocalDate.parse("2025-05-05"));
        r.setStatus("Generated");
        r.setPaymentMethod("Online");
        return r;
    }

    private RentInvoice existing(long id) {
        RentInvoice inv = new RentInvoice();
        inv.setInvoiceId(id);
        inv.setBillingMonth("2025-05");
        inv.setRentAmount(new BigDecimal("18000.00"));
        inv.setOtherCharges(new BigDecimal("500.00"));
        inv.setTotalAmount(new BigDecimal("18500.00"));
        inv.setDueDate(LocalDate.parse("2025-05-05"));
        inv.setStatus(InvoiceStatus.Generated);
        inv.setPaymentMethod("Online");
        return inv;
    }

    @Test
    void createInvoice_success_returnsResponse() {
        InvoiceResponse resp = service.createInvoice(validCreate());
        assertNotNull(resp);
        assertEquals("2025-05", resp.getBillingMonth());
    }

    @Test
    void createInvoice_savesEntity() {
        service.createInvoice(validCreate());
        verify(invoiceRepository).save(any(RentInvoice.class));
    }

    @Test
    void createInvoice_nullRequest_throwsBadRequest() {
        assertEquals("Request body is required",
                assertThrows(BadRequestException.class, () -> service.createInvoice(null)).getMessage());
    }

    @Test
    void createInvoice_missingBillingMonth_throws() {
        CreateInvoiceRequest r = validCreate();
        r.setBillingMonth(null);
        assertEquals("billingMonth is required",
                assertThrows(BadRequestException.class, () -> service.createInvoice(r)).getMessage());
    }

    @Test
    void createInvoice_missingRentAmount_throws() {
        CreateInvoiceRequest r = validCreate();
        r.setRentAmount(null);
        assertEquals("rentAmount is required",
                assertThrows(BadRequestException.class, () -> service.createInvoice(r)).getMessage());
    }

    @Test
    void createInvoice_defaultsOtherChargesToZero() {
        CreateInvoiceRequest r = validCreate();
        r.setOtherCharges(null);
        r.setTotalAmount(null);
        assertEquals(0, service.createInvoice(r).getOtherCharges().compareTo(BigDecimal.ZERO));
    }

    @Test
    void createInvoice_computesTotalWhenNull() {
        CreateInvoiceRequest r = validCreate();
        r.setTotalAmount(null);
        assertEquals(0, service.createInvoice(r).getTotalAmount().compareTo(new BigDecimal("18500.00")));
    }

    @Test
    void createInvoice_defaultsStatusToGenerated() {
        CreateInvoiceRequest r = validCreate();
        r.setStatus(null);
        assertEquals("Generated", service.createInvoice(r).getStatus());
    }

    @Test
    void createInvoice_invalidStatus_throws() {
        CreateInvoiceRequest r = validCreate();
        r.setStatus("Nope");
        assertEquals("Invalid invoice status: Nope",
                assertThrows(BadRequestException.class, () -> service.createInvoice(r)).getMessage());
    }

    @Test
    void getAllInvoices_returnsMappedList() {
        when(invoiceRepository.findAll()).thenReturn(List.of(existing(1), existing(2)));
        assertEquals(2, service.getAllInvoices().size());
    }

    @Test
    void getInvoiceById_found_returnsResponse() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(existing(1)));
        assertEquals(1L, service.getInvoiceById(1L).getInvoiceId());
    }

    @Test
    void getInvoiceById_notFound_throws() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(InvoiceNotFoundException.class, () -> service.getInvoiceById(99L));
    }

    @Test
    void updateInvoice_updatesStatus() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(existing(1)));
        UpdateInvoiceRequest r = new UpdateInvoiceRequest();
        r.setStatus("Paid");
        assertEquals("Paid", service.updateInvoice(1L, r).getStatus());
    }

    @Test
    void updateInvoice_notFound_throws() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(InvoiceNotFoundException.class,
                () -> service.updateInvoice(99L, new UpdateInvoiceRequest()));
    }
}
