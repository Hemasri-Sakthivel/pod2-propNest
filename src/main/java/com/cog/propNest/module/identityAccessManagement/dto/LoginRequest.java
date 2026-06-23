package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Body for {@code POST /IAM/auth/login}.
 */
public record LoginRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        String email,

        @NotBlank(message = "password is required")
        String password
) {
}
