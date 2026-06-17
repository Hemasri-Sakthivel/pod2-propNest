package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.module.identityAccessManagement.dto.AuditLogListResponse;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit-log review for admins. REAL_ESTATE_ADMIN only.
 *
 * <ul>
 *   <li>{@code GET /IAM/admin/audit-log}</li>
 *   <li>{@code GET /IAM/admin/audit-log/user/{userId}}</li>
 * </ul>
 */
@RestController
@RequestMapping("/IAM/admin/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AccessGuard accessGuard;

    public AuditLogController(AuditLogService auditLogService, AccessGuard accessGuard) {
        this.auditLogService = auditLogService;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    public ResponseEntity<AuditLogListResponse> getAllLogs() {
        accessGuard.requireAdmin();
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AuditLogListResponse> getLogsForUser(@PathVariable Long userId) {
        accessGuard.requireAdmin();
        return ResponseEntity.ok(auditLogService.getLogsForUser(userId));
    }
}
