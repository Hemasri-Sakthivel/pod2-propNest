package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.dto.AuditLogListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.AuditLogResponse;
import com.cog.propNest.module.identityAccessManagement.repository.AuditLogRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read access to audit logs for admin review.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AuditLogListResponse getAllLogs() {
        List<AuditLogResponse> logs = auditLogRepository.findAllByOrderByTimeStampDesc().stream()
                .map(AuditLogResponse::from)
                .toList();
        return new AuditLogListResponse(logs);
    }

    @Transactional(readOnly = true)
    public AuditLogListResponse getLogsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        List<AuditLogResponse> logs = auditLogRepository.findByUserIdOrderByTimeStampDesc(userId).stream()
                .map(AuditLogResponse::from)
                .toList();
        return new AuditLogListResponse(logs);
    }
}
