package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating a new monthly rent invoice.
 */
@Data
public class CreateInvoiceRequest {

    private Long leaseId;
    private Long tenantId;
    private String billingMonth;
    private BigDecimal rentAmount;
    private BigDecimal otherCharges;
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private String status;
    private String paymentMethod;
}
