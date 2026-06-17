package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for {@code PUT /IAM/admin/users/{userId}/status}. Allowed values:
 * {@code A} (Active), {@code I} (Inactive), {@code S} (Suspended).
 */
public record UpdateStatusRequest(
        @NotBlank(message = "status is required")
        String status
) {
}
