package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Payload for updating an invoice's status or charge amounts. All fields are
 * optional; only non-null values are applied.
 */
@Data
public class UpdateInvoiceRequest {

    private String status;
    private BigDecimal otherCharges;
    private BigDecimal totalAmount;
    private String paymentMethod;
}
