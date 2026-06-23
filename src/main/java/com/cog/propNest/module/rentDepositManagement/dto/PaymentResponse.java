package com.cog.propNest.module.rentDepositManagement.dto;

import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model returned for rent payment queries.
 */
@Data
public class PaymentResponse {

    private Long paymentId;
    private Long invoiceId;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    private String method;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(RentPayment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.paymentId = payment.getPaymentId();
        dto.invoiceId = payment.getInvoiceId();
        dto.paidAmount = payment.getPaidAmount();
        dto.paymentDate = payment.getPaymentDate();
        dto.method = payment.getMethod() != null ? payment.getMethod().name() : null;
        dto.status = payment.getStatus() != null ? payment.getStatus().name() : null;
        dto.remarks = payment.getRemarks();
        dto.createdAt = payment.getCreatedAt();
        dto.updatedAt = payment.getUpdatedAt();
        return dto;
    }
}
