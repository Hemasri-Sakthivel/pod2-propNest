package com.cog.propNest.module.tenantOnboardingLease.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lease_agreement")
public class LeaseAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaseId")
    private Long leaseId;

    @Column(name = "unitId", nullable = false)
    private Integer unitId;

    @Column(name = "tenantId", nullable = false)
    private Long tenantId;

    @Column(name = "ownerId", nullable = false)
    private Long ownerId;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthlyRent", nullable = false)
    private Double monthlyRent;

    @Column(name = "securityDeposit", nullable = false)
    private Double securityDeposit;

    @Column(name = "escalationPercent")
    private Double escalationPercent = 0.0;

    @Column(name = "noticePeriodDays")
    private Integer noticePeriodDays = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaseStatus status = LeaseStatus.D;

    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum LeaseStatus {
        D, A, E, T, R
    }

    public Long getLeaseId() { return leaseId; }
    public void setLeaseId(Long leaseId) { this.leaseId = leaseId; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(Double monthlyRent) { this.monthlyRent = monthlyRent; }

    public Double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(Double securityDeposit) { this.securityDeposit = securityDeposit; }

    public Double getEscalationPercent() { return escalationPercent; }
    public void setEscalationPercent(Double escalationPercent) { this.escalationPercent = escalationPercent; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public LeaseStatus getStatus() { return status; }
    public void setStatus(LeaseStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}