package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for recording a payment against an invoice.
 */
@Data
public class CreatePaymentRequest {

    private Long invoiceId;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    private String method;
    private String status;
    private String remarks;
}
