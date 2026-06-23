package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Payload for processing a full deposit refund at tenancy end.
 */
@Data
public class ProcessRefundRequest {

    private LocalDate refundDate;
    private String remarks;
}
