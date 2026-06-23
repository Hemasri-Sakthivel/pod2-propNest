package com.cog.propNest.module.identityAccessManagement.dto;

/**
 * Body returned by {@code POST /IAM/auth/refresh-token}.
 */
public record AccessTokenResponse(
        String accessToken
) {
}
