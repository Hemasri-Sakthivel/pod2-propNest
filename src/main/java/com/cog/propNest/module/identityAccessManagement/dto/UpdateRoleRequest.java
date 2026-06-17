package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Body for {@code PUT /IAM/admin/users/{userId}/role}. {@code roleId} must be a
 * valid id from {@code GET /IAM/admin/roles}.
 */
public record UpdateRoleRequest(
        @NotNull(message = "roleId is required")
        Integer roleId
) {
}
