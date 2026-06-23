package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.exception.AccountNotActiveException;
import com.cog.propNest.module.identityAccessManagement.exception.EmailAlreadyExistsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidCredentialsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRefreshTokenException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRoleException;
import com.cog.propNest.module.identityAccessManagement.dto.AccessTokenResponse;
import com.cog.propNest.module.identityAccessManagement.dto.LoginRequest;
import com.cog.propNest.module.identityAccessManagement.dto.LoginResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.SessionStatus;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserSession;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserSessionRepository;
import com.cog.propNest.module.identityAccessManagement.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Public authentication flows: register, login, logout and refresh-token.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserSessionRepository userSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new InvalidRoleException(request.roleId()));
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.builder()
                .role(role)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.A)
                .build();
        user = userRepository.save(user);
        auditService.record(user.getUserId(), AuditActions.USER_REGISTERED);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() != UserStatus.A) {
            throw new AccountNotActiveException();
        }

        String refreshToken = jwtService.generateRefreshToken();
        UserSession session = UserSession.builder()
                .userId(user.getUserId())
                .refreshToken(refreshToken)
                .createdAt(Instant.now())
                .refreshTokenExpiryTime(jwtService.refreshTokenExpiry())
                .status(SessionStatus.ACTIVE)
                .build();
        userSessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(user);
        auditService.record(user.getUserId(), AuditActions.USER_LOGIN);

        return new LoginResponse(accessToken, refreshToken, user.getUserId(),
                user.getRole().getRoleName());
    }

    @Transactional
    public void logout(Long userId, String refreshToken) {
        userSessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
        });
        auditService.record(userId, AuditActions.USER_LOGOUT);
    }

    @Transactional
    public AccessTokenResponse refreshToken(String refreshToken) {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (session.getStatus() != SessionStatus.ACTIVE
                || session.getRefreshTokenExpiryTime().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        User user = userRepository.findById(session.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        String accessToken = jwtService.generateAccessToken(user);
        auditService.record(user.getUserId(), AuditActions.TOKEN_REFRESHED);
        return new AccessTokenResponse(accessToken);
    }
}
