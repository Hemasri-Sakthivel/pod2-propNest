package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code POST /IAM/auth/register} and the basis for admin user
 * creation. {@code roleId} maps to one of the six PropNest roles.
 */
public record RegisterRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        @Size(max = 150, message = "email must be at most 150 characters")
        String email,

        @Pattern(regexp = "\\d{10,15}", message = "phone must be 10-15 digits")
        String phone,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be 8-100 characters")
        String password,

        @NotNull(message = "roleId is required")
        Integer roleId
) {
}
