package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.module.identityAccessManagement.dto.RoleListResponse;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role lookup. {@code GET /IAM/admin/roles} — REAL_ESTATE_ADMIN only.
 */
@RestController
@RequestMapping("/IAM/admin/roles")
public class RoleController {

    private final RoleService roleService;
    private final AccessGuard accessGuard;

    public RoleController(RoleService roleService, AccessGuard accessGuard) {
        this.roleService = roleService;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    public ResponseEntity<RoleListResponse> getAllRoles() {
        accessGuard.requireAdmin();
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
