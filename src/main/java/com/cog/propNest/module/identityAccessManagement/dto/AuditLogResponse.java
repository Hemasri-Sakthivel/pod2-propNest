package com.cog.propNest.module.identityAccessManagement.dto;

import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;

import java.time.Instant;

/**
 * A single audit-log entry.
 */
public record AuditLogResponse(
        Long auditId,
        Long userId,
        String action,
        String entityType,
        Instant timeStamp
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getAuditId(),
                log.getUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getTimeStamp()
        );
    }
}
