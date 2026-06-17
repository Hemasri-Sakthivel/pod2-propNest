package com.cog.propNest.module.rentDepositManagement.dto;

import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model returned for security deposit queries.
 */
@Data
public class DepositResponse {

    private Long depositId;
    private Long leaseId;
    private Long tenantId;
    private BigDecimal depositAmount;
    private LocalDate receivedDate;
    private String deductionsJSON;
    private BigDecimal refundAmount;
    private LocalDate refundDate;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepositResponse from(SecurityDepositLedger deposit) {
        DepositResponse dto = new DepositResponse();
        dto.depositId = deposit.getDepositId();
        dto.leaseId = deposit.getLeaseId();
        dto.tenantId = deposit.getTenantId();
        dto.depositAmount = deposit.getDepositAmount();
        dto.receivedDate = deposit.getReceivedDate();
        dto.deductionsJSON = deposit.getDeductionsJSON();
        dto.refundAmount = deposit.getRefundAmount();
        dto.refundDate = deposit.getRefundDate();
        dto.status = deposit.getStatus() != null ? deposit.getStatus().name() : null;
        dto.remarks = deposit.getRemarks();
        dto.createdAt = deposit.getCreatedAt();
        dto.updatedAt = deposit.getUpdatedAt();
        return dto;
    }
}
