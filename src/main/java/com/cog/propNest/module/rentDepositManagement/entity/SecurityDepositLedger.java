package com.cog.propNest.module.rentDepositManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ledger entry tracking a tenant's security deposit through its lifecycle
 * (held, partially refunded, fully refunded). Maps to the
 * {@code security_deposit_ledger} table.
 */
@Entity
@Table(name = "security_deposit_ledger")
@Getter
@Setter
@NoArgsConstructor
public class SecurityDepositLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long depositId;

    @Column(nullable = false)
    private Long leaseId;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(columnDefinition = "json")
    private String deductionsJSON;

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    private LocalDate refundDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status = DepositStatus.Held;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.refundAmount == null) {
            this.refundAmount = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = DepositStatus.Held;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
