package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.module.identityAccessManagement.dto.MessageResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateRoleRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateStatusRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UserListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user-management endpoints. All require the REAL_ESTATE_ADMIN role,
 * enforced via {@link AccessGuard}.
 *
 * <ul>
 *   <li>{@code POST /IAM/admin/users}</li>
 *   <li>{@code GET  /IAM/admin/users} (?role=&amp;status=)</li>
 *   <li>{@code GET  /IAM/admin/users/{userId}}</li>
 *   <li>{@code PUT  /IAM/admin/users/{userId}/status}</li>
 *   <li>{@code PUT  /IAM/admin/users/{userId}/role}</li>
 * </ul>
 */
@RestController
@RequestMapping("/IAM/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AccessGuard accessGuard;

    public AdminUserController(AdminUserService adminUserService, AccessGuard accessGuard) {
        this.adminUserService = adminUserService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        accessGuard.requireAdmin();
        adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("User created successfully"));
    }

    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status) {
        accessGuard.requireAdmin();
        return ResponseEntity.ok(adminUserService.getAllUsers(role, status));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        accessGuard.requireAdmin();
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long userId,
                                                        @Valid @RequestBody UpdateStatusRequest request) {
        accessGuard.requireAdmin();
        adminUserService.updateStatus(userId, request);
        return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<MessageResponse> updateRole(@PathVariable Long userId,
                                                      @Valid @RequestBody UpdateRoleRequest request) {
        accessGuard.requireAdmin();
        adminUserService.updateRole(userId, request);
        return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
    }
}
