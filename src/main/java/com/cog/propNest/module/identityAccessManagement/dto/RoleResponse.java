package com.cog.propNest.module.identityAccessManagement.dto;

import com.cog.propNest.module.identityAccessManagement.entity.Role;

/**
 * A single role entry.
 */
public record RoleResponse(
        Integer roleId,
        String roleName
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getRoleId(), role.getRoleName());
    }
}
