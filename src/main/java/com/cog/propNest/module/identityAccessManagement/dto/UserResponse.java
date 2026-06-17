package com.cog.propNest.module.identityAccessManagement.dto;

import com.cog.propNest.module.identityAccessManagement.entity.User;

/**
 * User profile view. The password hash is never exposed.
 */
public record UserResponse(
        Long userId,
        String name,
        String email,
        String phone,
        String role,
        String status
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getRoleName(),
                user.getStatus().name()
        );
    }
}
