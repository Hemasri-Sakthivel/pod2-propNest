package com.cog.propNest.module.identityAccessManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code PUT /IAM/users/me}. A user may change only their own name and
 * phone; email and role are immutable here.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Pattern(regexp = "\\d{10,15}", message = "phone must be 10-15 digits")
        String phone
) {
}
