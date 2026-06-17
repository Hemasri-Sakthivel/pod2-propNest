package com.cog.propNest.module.tenantOnboardingLease.dto;

public class TenantKycDTO {

    private Long tenantId;
    private String documentType;
    private String documentRef;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentRef() { return documentRef; }
    public void setDocumentRef(String documentRef) { this.documentRef = documentRef; }
}