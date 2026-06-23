package com.cog.propNest.module.tenantOnboardingLease.dto;

import java.time.LocalDate;

public class LeaseAgreementDTO {

    private Integer unitId;
    private Long tenantId;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private Double securityDeposit;
    private Double escalationPercent;
    private Integer noticePeriodDays;

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
}