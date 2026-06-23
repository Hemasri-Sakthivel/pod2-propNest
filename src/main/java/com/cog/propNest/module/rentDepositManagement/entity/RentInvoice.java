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
 * A monthly rent invoice raised against an active lease. Maps to the
 * {@code rent_invoice} table.
 */
@Entity
@Table(name = "rent_invoice")
@Getter
@Setter
@NoArgsConstructor
public class RentInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(nullable = false)
    private Long leaseId;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 7)
    private String billingMonth;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rentAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.Generated;

    @Column(length = 50)
    private String paymentMethod;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.otherCharges == null) {
            this.otherCharges = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = InvoiceStatus.Generated;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
