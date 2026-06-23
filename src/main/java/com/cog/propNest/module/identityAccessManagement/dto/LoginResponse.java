package com.cog.propNest.module.identityAccessManagement.dto;

/**
 * Body returned by {@code POST /IAM/auth/login}.
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String role
) {
}
