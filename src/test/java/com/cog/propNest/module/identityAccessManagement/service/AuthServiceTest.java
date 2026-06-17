package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.AccessTokenResponse;
import com.cog.propNest.module.identityAccessManagement.dto.LoginRequest;
import com.cog.propNest.module.identityAccessManagement.dto.LoginResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.SessionStatus;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserSession;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.exception.AccountNotActiveException;
import com.cog.propNest.module.identityAccessManagement.exception.EmailAlreadyExistsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidCredentialsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRefreshTokenException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRoleException;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserSessionRepository;
import com.cog.propNest.module.identityAccessManagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserSessionRepository userSessionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuditService auditService;

    @InjectMocks private AuthService authService;

    private Role tenantRole;
    private User activeUser;

    @BeforeEach
    void setUp() {
        tenantRole = new Role(2, "TENANT");
        activeUser = User.builder()
                .userId(101L)
                .role(tenantRole)
                .name("Hemasri S")
                .email("hemasri@propnest.com")
                .phone("9876543210")
                .passwordHash("HASH")
                .status(UserStatus.A)
                .build();
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("Hemasri S", "hemasri@propnest.com",
                "9876543210", "Secret@123", 2);
    }

    // ---------------------------------------------------------------- register

    @Test
    @DisplayName("register persists a new active user and audits it")
    void register_success() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase("hemasri@propnest.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        authService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("hemasri@propnest.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASH");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.A);
        assertThat(saved.getRole()).isEqualTo(tenantRole);
        verify(auditService).record(101L, AuditActions.USER_REGISTERED);
    }

    @Test
    @DisplayName("register rejects an unknown roleId with InvalidRoleException")
    void register_invalidRole() {
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(InvalidRoleException.class);

        verify(userRepository, never()).save(any());
        verify(auditService, never()).record(any(), anyString());
    }

    @Test
    @DisplayName("register rejects a duplicate email with EmailAlreadyExistsException")
    void register_duplicateEmail() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase("hemasri@propnest.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register hashes the raw password before saving")
    void register_hashesPassword() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("BCRYPTED");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        authService.register(registerRequest());

        verify(passwordEncoder).encode("Secret@123");
    }

    // ------------------------------------------------------------------- login

    @Test
    @DisplayName("login returns tokens, userId and role on valid credentials")
    void login_success() {
        when(userRepository.findByEmailIgnoreCase("hemasri@propnest.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Secret@123", "HASH")).thenReturn(true);
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        when(jwtService.refreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(3600));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");

        LoginResponse response = authService.login(
                new LoginRequest("hemasri@propnest.com", "Secret@123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.userId()).isEqualTo(101L);
        assertThat(response.role()).isEqualTo("TENANT");
        verify(auditService).record(101L, AuditActions.USER_LOGIN);
    }

    @Test
    @DisplayName("login persists an ACTIVE session holding the refresh token")
    void login_persistsSession() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        Instant expiry = Instant.now().plusSeconds(3600);
        when(jwtService.refreshTokenExpiry()).thenReturn(expiry);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");

        authService.login(new LoginRequest("hemasri@propnest.com", "Secret@123"));

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(captor.capture());
        UserSession session = captor.getValue();
        assertThat(session.getUserId()).isEqualTo(101L);
        assertThat(session.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getRefreshTokenExpiryTime()).isEqualTo(expiry);
    }

    @Test
    @DisplayName("login fails with InvalidCredentialsException when email is unknown")
    void login_unknownEmail() {
        when(userRepository.findByEmailIgnoreCase("nobody@propnest.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@propnest.com", "x")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("login fails with InvalidCredentialsException when password mismatches")
    void login_wrongPassword() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("hemasri@propnest.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login fails with AccountNotActiveException for a suspended user")
    void login_suspended() {
        activeUser.setStatus(UserStatus.S);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("hemasri@propnest.com", "Secret@123")))
                .isInstanceOf(AccountNotActiveException.class);

        verify(userSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("login fails with AccountNotActiveException for an inactive user")
    void login_inactive() {
        activeUser.setStatus(UserStatus.I);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("hemasri@propnest.com", "Secret@123")))
                .isInstanceOf(AccountNotActiveException.class);
    }

    // ------------------------------------------------------------------ logout

    @Test
    @DisplayName("logout revokes the matching session and audits it")
    void logout_revokesSession() {
        UserSession session = UserSession.builder()
                .sessionId(5L).userId(101L).refreshToken("refresh-token")
                .createdAt(Instant.now()).refreshTokenExpiryTime(Instant.now().plusSeconds(60))
                .status(SessionStatus.ACTIVE).build();
        when(userSessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(session));

        authService.logout(101L, "refresh-token");

        assertThat(session.getStatus()).isEqualTo(SessionStatus.REVOKED);
        verify(userSessionRepository).save(session);
        verify(auditService).record(101L, AuditActions.USER_LOGOUT);
    }

    @Test
    @DisplayName("logout is idempotent when the refresh token is unknown")
    void logout_unknownToken() {
        when(userSessionRepository.findByRefreshToken("ghost")).thenReturn(Optional.empty());

        authService.logout(101L, "ghost");

        verify(userSessionRepository, never()).save(any());
        verify(auditService).record(101L, AuditActions.USER_LOGOUT);
    }

    // ------------------------------------------------------------ refreshToken

    @Test
    @DisplayName("refreshToken issues a fresh access token for an active session")
    void refresh_success() {
        UserSession session = UserSession.builder()
                .sessionId(5L).userId(101L).refreshToken("refresh-token")
                .createdAt(Instant.now()).refreshTokenExpiryTime(Instant.now().plusSeconds(3600))
                .status(SessionStatus.ACTIVE).build();
        when(userSessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(session));
        when(userRepository.findById(101L)).thenReturn(Optional.of(activeUser));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");

        AccessTokenResponse response = authService.refreshToken("refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        verify(auditService).record(101L, AuditActions.TOKEN_REFRESHED);
    }

    @Test
    @DisplayName("refreshToken fails when the token is unknown")
    void refresh_unknownToken() {
        when(userSessionRepository.findByRefreshToken("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("ghost"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("refreshToken fails when the session has been revoked")
    void refresh_revokedSession() {
        UserSession session = UserSession.builder()
                .sessionId(5L).userId(101L).refreshToken("refresh-token")
                .createdAt(Instant.now()).refreshTokenExpiryTime(Instant.now().plusSeconds(3600))
                .status(SessionStatus.REVOKED).build();
        when(userSessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("refreshToken fails when the refresh token has expired")
    void refresh_expiredToken() {
        UserSession session = UserSession.builder()
                .sessionId(5L).userId(101L).refreshToken("refresh-token")
                .createdAt(Instant.now().minusSeconds(7200))
                .refreshTokenExpiryTime(Instant.now().minusSeconds(60))
                .status(SessionStatus.ACTIVE).build();
        when(userSessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("refreshToken fails when the owning user no longer exists")
    void refresh_userMissing() {
        UserSession session = UserSession.builder()
                .sessionId(5L).userId(101L).refreshToken("refresh-token")
                .createdAt(Instant.now()).refreshTokenExpiryTime(Instant.now().plusSeconds(3600))
                .status(SessionStatus.ACTIVE).build();
        when(userSessionRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(session));
        when(userRepository.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
