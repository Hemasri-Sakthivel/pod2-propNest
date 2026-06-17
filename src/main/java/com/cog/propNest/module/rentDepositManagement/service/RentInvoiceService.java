package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.dto.InvoiceResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.entity.InvoiceStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.repository.RentInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for managing monthly rent invoices.
 */
@Service
@Transactional
public class RentInvoiceService {

    private final RentInvoiceRepository invoiceRepository;

    public RentInvoiceService(RentInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        require(request.getLeaseId(), "leaseId");
        require(request.getTenantId(), "tenantId");
        require(request.getBillingMonth(), "billingMonth");
        require(request.getRentAmount(), "rentAmount");
        require(request.getDueDate(), "dueDate");

        RentInvoice invoice = new RentInvoice();
        invoice.setLeaseId(request.getLeaseId());
        invoice.setTenantId(request.getTenantId());
        invoice.setBillingMonth(request.getBillingMonth());
        invoice.setRentAmount(request.getRentAmount());

        BigDecimal otherCharges = request.getOtherCharges() != null
                ? request.getOtherCharges() : BigDecimal.ZERO;
        invoice.setOtherCharges(otherCharges);

        BigDecimal totalAmount = request.getTotalAmount() != null
                ? request.getTotalAmount() : request.getRentAmount().add(otherCharges);
        invoice.setTotalAmount(totalAmount);

        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(request.getStatus() != null
                ? parseStatus(request.getStatus()) : InvoiceStatus.Generated);
        invoice.setPaymentMethod(request.getPaymentMethod());

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        return InvoiceResponse.from(findInvoice(invoiceId));
    }

    public InvoiceResponse updateInvoice(Long invoiceId, UpdateInvoiceRequest request) {
        RentInvoice invoice = findInvoice(invoiceId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getStatus() != null) {
            invoice.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getOtherCharges() != null) {
            invoice.setOtherCharges(request.getOtherCharges());
        }
        if (request.getTotalAmount() != null) {
            invoice.setTotalAmount(request.getTotalAmount());
        }
        if (request.getPaymentMethod() != null) {
            invoice.setPaymentMethod(request.getPaymentMethod());
        }
        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    private RentInvoice findInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
    }

    private InvoiceStatus parseStatus(String status) {
        try {
            return InvoiceStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid invoice status: " + status);
        }
    }

    private void require(Object value, String field) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new BadRequestException(field + " is required");
        }
    }
}
