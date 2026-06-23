package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.exception.EmailAlreadyExistsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRoleException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidUserStatusException;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.dto.RegisterRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateRoleRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UpdateStatusRequest;
import com.cog.propNest.module.identityAccessManagement.dto.UserListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Admin (REAL_ESTATE_ADMIN) operations for managing all user accounts.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminUserService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public void createUser(RegisterRequest request) {
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
        auditService.record(user.getUserId(), AuditActions.USER_CREATED);
    }

    @Transactional(readOnly = true)
    public UserListResponse getAllUsers(String roleFilter, String statusFilter) {
        boolean hasRole = StringUtils.hasText(roleFilter);
        UserStatus status = StringUtils.hasText(statusFilter) ? parseStatus(statusFilter) : null;

        List<User> users;
        if (hasRole && status != null) {
            users = userRepository.findByRole_RoleNameIgnoreCaseAndStatus(roleFilter, status);
        } else if (hasRole) {
            users = userRepository.findByRole_RoleNameIgnoreCase(roleFilter);
        } else if (status != null) {
            users = userRepository.findByStatus(status);
        } else {
            users = userRepository.findAll();
        }

        List<UserResponse> body = users.stream().map(UserResponse::from).toList();
        return new UserListResponse(body);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return UserResponse.from(loadUser(userId));
    }

    @Transactional
    public void updateStatus(Long userId, UpdateStatusRequest request) {
        User user = loadUser(userId);
        user.setStatus(parseStatus(request.status()));
        userRepository.save(user);
        auditService.record(userId, AuditActions.USER_STATUS_UPDATED);
    }

    @Transactional
    public void updateRole(Long userId, UpdateRoleRequest request) {
        User user = loadUser(userId);
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new InvalidRoleException(request.roleId()));
        user.setRole(role);
        userRepository.save(user);
        auditService.record(userId, AuditActions.USER_ROLE_UPDATED);
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserStatus parseStatus(String value) {
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserStatusException(value);
        }
    }
}
