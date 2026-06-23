package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

/**
 * Payload for updating a payment's status or method. A payment that is already
 * {@code Received} cannot be modified.
 */
@Data
public class UpdatePaymentRequest {

    private String status;
    private String method;
    private String remarks;
}
