package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.module.identityAccessManagement.dto.AccessTokenResponse;
import com.cog.propNest.module.identityAccessManagement.dto.LoginRequest;
import com.cog.propNest.module.identityAccessManagement.dto.LoginResponse;
import com.cog.propNest.module.identityAccessManagement.dto.MessageResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RefreshTokenRequest;
import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth and token-management endpoints.
 *
 * <ul>
 *   <li>{@code POST /IAM/auth/register} — public</li>
 *   <li>{@code POST /IAM/auth/login} — public</li>
 *   <li>{@code POST /IAM/auth/logout} — requires access token</li>
 *   <li>{@code POST /IAM/auth/refresh-token} — semi-public (refresh token)</li>
 * </ul>
 */
@RestController
@RequestMapping("/IAM/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        Long userId = AuthContext.require().userId();
        authService.logout(userId, request.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }
}
