package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.ChangePasswordRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateProfileRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.exception.IncorrectPasswordException;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSelfService")
class UserSelfServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    @InjectMocks private UserSelfService userSelfService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(101L)
                .role(new Role(2, "TENANT"))
                .name("Hemasri S")
                .email("hemasri@propnest.com")
                .phone("9876543210")
                .passwordHash("HASH")
                .status(UserStatus.A)
                .build();
    }

    // ---------------------------------------------------------------- profile

    @Test
    @DisplayName("getProfile returns the mapped user view")
    void getProfile_success() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        UserResponse response = userSelfService.getProfile(101L);

        assertThat(response.userId()).isEqualTo(101L);
        assertThat(response.email()).isEqualTo("hemasri@propnest.com");
        assertThat(response.role()).isEqualTo("TENANT");
        assertThat(response.status()).isEqualTo("A");
    }

    @Test
    @DisplayName("getProfile throws UserNotFoundException for a missing user")
    void getProfile_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSelfService.getProfile(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile changes name and phone and audits it")
    void updateProfile_success() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        userSelfService.updateProfile(101L, new UpdateProfileRequest("Hemasri Sakthivel", "9876540000"));

        assertThat(user.getName()).isEqualTo("Hemasri Sakthivel");
        assertThat(user.getPhone()).isEqualTo("9876540000");
        verify(userRepository).save(user);
        verify(auditService).record(101L, AuditActions.PROFILE_UPDATED);
    }

    @Test
    @DisplayName("updateProfile does not change the email")
    void updateProfile_keepsEmail() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        userSelfService.updateProfile(101L, new UpdateProfileRequest("New Name", "9876540000"));

        assertThat(user.getEmail()).isEqualTo("hemasri@propnest.com");
    }

    @Test
    @DisplayName("updateProfile throws UserNotFoundException for a missing user")
    void updateProfile_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSelfService.updateProfile(999L,
                new UpdateProfileRequest("X", "9876540000")))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    // --------------------------------------------------------------- password

    @Test
    @DisplayName("changePassword updates the hash when current password matches")
    void changePassword_success() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "HASH")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("NEW_HASH");

        userSelfService.changePassword(101L, new ChangePasswordRequest("Secret@123", "NewPass@456"));

        assertThat(user.getPasswordHash()).isEqualTo("NEW_HASH");
        verify(userRepository).save(user);
        verify(auditService).record(101L, AuditActions.PASSWORD_CHANGED);
    }

    @Test
    @DisplayName("changePassword throws IncorrectPasswordException when current password is wrong")
    void changePassword_wrongCurrent() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> userSelfService.changePassword(101L,
                new ChangePasswordRequest("wrong", "NewPass@456")))
                .isInstanceOf(IncorrectPasswordException.class);

        verify(userRepository, never()).save(any());
        verify(auditService, never()).record(any(), anyString());
    }

    @Test
    @DisplayName("changePassword throws UserNotFoundException for a missing user")
    void changePassword_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSelfService.changePassword(999L,
                new ChangePasswordRequest("Secret@123", "NewPass@456")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("changePassword never stores the raw new password")
    void changePassword_encodesNewPassword() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("ENC");

        userSelfService.changePassword(101L, new ChangePasswordRequest("Secret@123", "NewPass@456"));

        verify(passwordEncoder).encode("NewPass@456");
        assertThat(user.getPasswordHash()).isNotEqualTo("NewPass@456");
    }
}
