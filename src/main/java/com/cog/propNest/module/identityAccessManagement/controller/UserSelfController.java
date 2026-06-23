package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.module.identityAccessManagement.dto.ChangePasswordRequest;
import com.cog.propNest.module.identityAccessManagement.dto.MessageResponse;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateProfileRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.service.UserSelfService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service endpoints for the authenticated user. Identity is taken from the
 * access token, never from a path parameter.
 *
 * <ul>
 *   <li>{@code GET /IAM/users/me}</li>
 *   <li>{@code PUT /IAM/users/me}</li>
 *   <li>{@code PUT /IAM/users/me/password}</li>
 * </ul>
 */
@RestController
@RequestMapping("/IAM/users/me")
public class UserSelfController {

    private final UserSelfService userSelfService;

    public UserSelfController(UserSelfService userSelfService) {
        this.userSelfService = userSelfService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile() {
        Long userId = AuthContext.require().userId();
        return ResponseEntity.ok(userSelfService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = AuthContext.require().userId();
        userSelfService.updateProfile(userId, request);
        return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = AuthContext.require().userId();
        userSelfService.changePassword(userId, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
