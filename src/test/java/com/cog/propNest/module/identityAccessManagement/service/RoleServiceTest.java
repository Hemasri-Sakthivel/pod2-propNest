package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.RoleListResponse;
import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService")
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;

    @InjectMocks private RoleService roleService;

    @Test
    @DisplayName("getAllRoles returns all roles mapped to the response")
    void getAllRoles_success() {
        when(roleRepository.findAll(any(Sort.class))).thenReturn(List.of(
                new Role(1, "OWNER"),
                new Role(2, "TENANT"),
                new Role(6, "REAL_ESTATE_ADMIN")));

        RoleListResponse response = roleService.getAllRoles();

        assertThat(response.roles()).hasSize(3);
        assertThat(response.roles().get(0).roleId()).isEqualTo(1);
        assertThat(response.roles().get(0).roleName()).isEqualTo("OWNER");
        assertThat(response.roles().get(2).roleName()).isEqualTo("REAL_ESTATE_ADMIN");
    }

    @Test
    @DisplayName("getAllRoles requests a sorted result")
    void getAllRoles_sorted() {
        when(roleRepository.findAll(any(Sort.class))).thenReturn(List.of());

        roleService.getAllRoles();

        verify(roleRepository).findAll(Sort.by("roleId"));
    }

    @Test
    @DisplayName("getAllRoles returns an empty list when no roles exist")
    void getAllRoles_empty() {
        when(roleRepository.findAll(any(Sort.class))).thenReturn(List.of());

        RoleListResponse response = roleService.getAllRoles();

        assertThat(response.roles()).isEmpty();
    }
}
