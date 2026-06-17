package com.cog.propNest.module.identityAccessManagement.dto;

import java.util.List;

/**
 * Body returned by {@code GET /IAM/admin/roles}: {@code { "roles": [ ... ] }}.
 */
public record RoleListResponse(
        List<RoleResponse> roles
) {
}
