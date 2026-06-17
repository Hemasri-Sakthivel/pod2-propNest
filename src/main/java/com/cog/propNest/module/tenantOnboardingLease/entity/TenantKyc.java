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
@Table(name = "tenant_kyc")
public class TenantKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kycId")
    private Long kycId;

    @Column(name = "tenantId", nullable = false)
    private Long tenantId;

    // Unique business identifier of the stored document. The physical file is
    // named after this id so the right document of the respective tenant can
    // always be located, and it is kept in the DB against the tenantId.
    @Column(name = "documentId", unique = true)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "documentType", nullable = false)
    private DocumentType documentType;

    @Column(name = "documentRef", nullable = false)
    private String documentRef;

    // Size of the stored file in bytes. Used to enforce the portal's total
    // storage capacity across all uploaded KYC documents.
    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "cerifiedDate")
    private LocalDate cerifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private KycStatus status = KycStatus.P;

    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DocumentType {
        NationalID, Passport, EmploymentLetter, BankStatement
    }

    public enum KycStatus {
        P, V, R
    }

    public Long getKycId() { return kycId; }
    public void setKycId(Long kycId) { this.kycId = kycId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getDocumentRef() { return documentRef; }
    public void setDocumentRef(String documentRef) { this.documentRef = documentRef; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDate getCerifiedDate() { return cerifiedDate; }
    public void setCerifiedDate(LocalDate cerifiedDate) { this.cerifiedDate = cerifiedDate; }

    public KycStatus getStatus() { return status; }
    public void setStatus(KycStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}