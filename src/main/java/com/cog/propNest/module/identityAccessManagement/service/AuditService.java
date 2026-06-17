package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;
import com.cog.propNest.module.identityAccessManagement.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Writes immutable audit-log entries for security-relevant actions.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Record an action performed against a user record.
     *
     * @param userId the user the action relates to
     * @param action one of {@link AuditActions}
     */
    public void record(Long userId, String action) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(AuditActions.ENTITY_USER)
                .timeStamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}
