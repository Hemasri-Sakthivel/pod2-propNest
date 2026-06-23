package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RoleRepository")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("save and findById round-trips a role")
    void saveAndFind() {
        roleRepository.save(new Role(6, "REAL_ESTATE_ADMIN"));

        Optional<Role> found = roleRepository.findById(6);

        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("REAL_ESTATE_ADMIN");
    }

    @Test
    @DisplayName("findByRoleNameIgnoreCase matches regardless of case")
    void findByRoleNameIgnoreCase() {
        roleRepository.save(new Role(2, "TENANT"));

        assertThat(roleRepository.findByRoleNameIgnoreCase("tenant")).isPresent();
        assertThat(roleRepository.findByRoleNameIgnoreCase("TENANT")).isPresent();
    }

    @Test
    @DisplayName("findByRoleNameIgnoreCase returns empty for an unknown role")
    void findByRoleNameIgnoreCase_unknown() {
        assertThat(roleRepository.findByRoleNameIgnoreCase("GHOST")).isEmpty();
    }

    @Test
    @DisplayName("findById returns empty for an unknown id")
    void findById_unknown() {
        assertThat(roleRepository.findById(999)).isEmpty();
    }
}
