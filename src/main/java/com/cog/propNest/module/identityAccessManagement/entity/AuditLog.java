package com.cog.propNest.module.identityAccessManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Immutable record of a security-relevant action (login, password change, role
 * or status update, etc.) used for regulatory audits.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditId")
    private Long auditId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entityType", nullable = false, length = 100)
    private String entityType;

    @Column(name = "timeStamp", nullable = false)
    private Instant timeStamp;
}
