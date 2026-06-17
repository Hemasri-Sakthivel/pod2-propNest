package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for {@code POST /IAM/auth/refresh-token} and {@code POST /IAM/auth/logout}.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
