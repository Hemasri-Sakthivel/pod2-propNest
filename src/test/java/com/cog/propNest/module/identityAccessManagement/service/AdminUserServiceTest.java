package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateRoleRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateStatusRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UserListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.exception.EmailAlreadyExistsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRoleException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidUserStatusException;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService")
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    @InjectMocks private AdminUserService adminUserService;

    private Role tenantRole;
    private Role managerRole;
    private User user;

    @BeforeEach
    void setUp() {
        tenantRole = new Role(2, "TENANT");
        managerRole = new Role(3, "PROPERTY_MANAGER");
        user = User.builder()
                .userId(101L).role(tenantRole).name("Ravi Kumar")
                .email("ravi@propnest.com").phone("9123456789")
                .passwordHash("HASH").status(UserStatus.A).build();
    }

    private RegisterRequest createRequest() {
        return new RegisterRequest("Ravi Kumar", "ravi@propnest.com",
                "9123456789", "Temp@1234", 2);
    }

    // --------------------------------------------------------------- create

    @Test
    @DisplayName("createUser persists the user and audits USER_CREATED")
    void createUser_success() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase("ravi@propnest.com")).thenReturn(false);
        when(passwordEncoder.encode("Temp@1234")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenReturn(user);

        adminUserService.createUser(createRequest());

        verify(userRepository).save(any(User.class));
        verify(auditService).record(101L, AuditActions.USER_CREATED);
    }

    @Test
    @DisplayName("createUser rejects an unknown roleId")
    void createUser_invalidRole() {
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.createUser(createRequest()))
                .isInstanceOf(InvalidRoleException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser rejects a duplicate email")
    void createUser_duplicateEmail() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase("ravi@propnest.com")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(createRequest()))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------- getAllUsers

    @Test
    @DisplayName("getAllUsers with no filters returns every user")
    void getAllUsers_noFilter() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        UserListResponse response = adminUserService.getAllUsers(null, null);

        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).email()).isEqualTo("ravi@propnest.com");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers filters by role only")
    void getAllUsers_byRole() {
        when(userRepository.findByRole_RoleNameIgnoreCase("TENANT")).thenReturn(List.of(user));

        UserListResponse response = adminUserService.getAllUsers("TENANT", null);

        assertThat(response.users()).hasSize(1);
        verify(userRepository).findByRole_RoleNameIgnoreCase("TENANT");
    }

    @Test
    @DisplayName("getAllUsers filters by status only")
    void getAllUsers_byStatus() {
        when(userRepository.findByStatus(UserStatus.A)).thenReturn(List.of(user));

        UserListResponse response = adminUserService.getAllUsers(null, "A");

        assertThat(response.users()).hasSize(1);
        verify(userRepository).findByStatus(UserStatus.A);
    }

    @Test
    @DisplayName("getAllUsers filters by both role and status")
    void getAllUsers_byRoleAndStatus() {
        when(userRepository.findByRole_RoleNameIgnoreCaseAndStatus("TENANT", UserStatus.A))
                .thenReturn(List.of(user));

        UserListResponse response = adminUserService.getAllUsers("TENANT", "A");

        assertThat(response.users()).hasSize(1);
        verify(userRepository).findByRole_RoleNameIgnoreCaseAndStatus("TENANT", UserStatus.A);
    }

    @Test
    @DisplayName("getAllUsers blank filters behave like no filter")
    void getAllUsers_blankFilters() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        UserListResponse response = adminUserService.getAllUsers("   ", "");

        assertThat(response.users()).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers rejects an invalid status value")
    void getAllUsers_invalidStatus() {
        assertThatThrownBy(() -> adminUserService.getAllUsers(null, "X"))
                .isInstanceOf(InvalidUserStatusException.class);
    }

    @Test
    @DisplayName("getAllUsers returns an empty list when nothing matches")
    void getAllUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        UserListResponse response = adminUserService.getAllUsers(null, null);

        assertThat(response.users()).isEmpty();
    }

    // -------------------------------------------------------------- getUserById

    @Test
    @DisplayName("getUserById returns the mapped user")
    void getUserById_success() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        UserResponse response = adminUserService.getUserById(101L);

        assertThat(response.userId()).isEqualTo(101L);
        assertThat(response.name()).isEqualTo("Ravi Kumar");
    }

    @Test
    @DisplayName("getUserById throws UserNotFoundException when absent")
    void getUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // -------------------------------------------------------------- updateStatus

    @Test
    @DisplayName("updateStatus to S suspends the user and audits it")
    void updateStatus_suspend() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        adminUserService.updateStatus(101L, new UpdateStatusRequest("S"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.S);
        verify(userRepository).save(user);
        verify(auditService).record(101L, AuditActions.USER_STATUS_UPDATED);
    }

    @Test
    @DisplayName("updateStatus to I marks the user inactive")
    void updateStatus_inactive() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        adminUserService.updateStatus(101L, new UpdateStatusRequest("I"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.I);
    }

    @Test
    @DisplayName("updateStatus accepts a lower-case value")
    void updateStatus_lowerCase() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        adminUserService.updateStatus(101L, new UpdateStatusRequest("a"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.A);
    }

    @Test
    @DisplayName("updateStatus rejects an invalid status value")
    void updateStatus_invalid() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminUserService.updateStatus(101L, new UpdateStatusRequest("X")))
                .isInstanceOf(InvalidUserStatusException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus throws UserNotFoundException when user is absent")
    void updateStatus_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateStatus(999L, new UpdateStatusRequest("A")))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --------------------------------------------------------------- updateRole

    @Test
    @DisplayName("updateRole reassigns the user and audits it")
    void updateRole_success() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(3)).thenReturn(Optional.of(managerRole));

        adminUserService.updateRole(101L, new UpdateRoleRequest(3));

        assertThat(user.getRole()).isEqualTo(managerRole);
        verify(userRepository).save(user);
        verify(auditService).record(101L, AuditActions.USER_ROLE_UPDATED);
    }

    @Test
    @DisplayName("updateRole rejects an unknown roleId")
    void updateRole_invalidRole() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateRole(101L, new UpdateRoleRequest(99)))
                .isInstanceOf(InvalidRoleException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole throws UserNotFoundException when user is absent")
    void updateRole_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateRole(999L, new UpdateRoleRequest(3)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("createUser encodes the password and never stores it raw")
    void createUser_encodesPassword() {
        when(roleRepository.findById(2)).thenReturn(Optional.of(tenantRole));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Temp@1234")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenReturn(user);

        adminUserService.createUser(createRequest());

        verify(passwordEncoder).encode("Temp@1234");
    }
}
