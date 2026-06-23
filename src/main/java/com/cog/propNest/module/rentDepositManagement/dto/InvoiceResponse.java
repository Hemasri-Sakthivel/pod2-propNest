package com.cog.propNest.module.rentDepositManagement.dto;

import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model returned for rent invoice queries.
 */
@Data
public class InvoiceResponse {

    private Long invoiceId;
    private Long leaseId;
    private Long tenantId;
    private String billingMonth;
    private BigDecimal rentAmount;
    private BigDecimal otherCharges;
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceResponse from(RentInvoice invoice) {
        InvoiceResponse dto = new InvoiceResponse();
        dto.invoiceId = invoice.getInvoiceId();
        dto.leaseId = invoice.getLeaseId();
        dto.tenantId = invoice.getTenantId();
        dto.billingMonth = invoice.getBillingMonth();
        dto.rentAmount = invoice.getRentAmount();
        dto.otherCharges = invoice.getOtherCharges();
        dto.totalAmount = invoice.getTotalAmount();
        dto.dueDate = invoice.getDueDate();
        dto.status = invoice.getStatus() != null ? invoice.getStatus().name() : null;
        dto.paymentMethod = invoice.getPaymentMethod();
        dto.createdAt = invoice.getCreatedAt();
        dto.updatedAt = invoice.getUpdatedAt();
        return dto;
    }
}
