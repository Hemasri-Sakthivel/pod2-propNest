package com.cog.propNest.module.identityAccessManagement.dto;

import java.util.List;

/**
 * Body returned by the audit-log endpoints: {@code { "logs": [ ... ] }}.
 */
public record AuditLogListResponse(
        List<AuditLogResponse> logs
) {
}
