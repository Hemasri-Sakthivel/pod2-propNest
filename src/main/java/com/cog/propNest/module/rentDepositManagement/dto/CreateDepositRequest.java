package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for recording a new security deposit at lease commencement.
 */
@Data
public class CreateDepositRequest {

    private Long leaseId;
    private Long tenantId;
    private BigDecimal depositAmount;
    private LocalDate receivedDate;
    private String status;
    private String remarks;
}
