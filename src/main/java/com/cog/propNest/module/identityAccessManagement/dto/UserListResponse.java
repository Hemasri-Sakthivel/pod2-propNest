package com.cog.propNest.module.identityAccessManagement.dto;

import java.util.List;

/**
 * Body returned by {@code GET /IAM/admin/users}: {@code { "users": [ ... ] }}.
 */
public record UserListResponse(
        List<UserResponse> users
) {
}
