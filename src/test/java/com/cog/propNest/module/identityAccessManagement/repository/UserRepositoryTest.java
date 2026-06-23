package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    private Role tenantRole;
    private Role ownerRole;

    @BeforeEach
    void setUp() {
        tenantRole = roleRepository.save(new Role(2, "TENANT"));
        ownerRole = roleRepository.save(new Role(1, "OWNER"));
    }

    private User newUser(String email, Role role, UserStatus status) {
        return User.builder()
                .role(role).name("Test User").email(email).phone("9999999999")
                .passwordHash("HASH").status(status).build();
    }

    @Test
    @DisplayName("save assigns an id and persists the user")
    void save_assignsId() {
        User saved = userRepository.save(newUser("a@propnest.com", tenantRole, UserStatus.A));

        assertThat(saved.getUserId()).isNotNull();
        assertThat(userRepository.findById(saved.getUserId())).isPresent();
    }

    @Test
    @DisplayName("findByEmailIgnoreCase finds the user regardless of case")
    void findByEmailIgnoreCase() {
        userRepository.save(newUser("hemasri@propnest.com", tenantRole, UserStatus.A));

        assertThat(userRepository.findByEmailIgnoreCase("HEMASRI@propnest.com")).isPresent();
        assertThat(userRepository.findByEmailIgnoreCase("hemasri@propnest.com")).isPresent();
    }

    @Test
    @DisplayName("findByEmailIgnoreCase returns empty for an unknown email")
    void findByEmailIgnoreCase_unknown() {
        assertThat(userRepository.findByEmailIgnoreCase("ghost@propnest.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase reflects presence")
    void existsByEmailIgnoreCase() {
        userRepository.save(newUser("ravi@propnest.com", tenantRole, UserStatus.A));

        assertThat(userRepository.existsByEmailIgnoreCase("RAVI@propnest.com")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("none@propnest.com")).isFalse();
    }

    @Test
    @DisplayName("a duplicate email violates the unique constraint")
    void duplicateEmail_rejected() {
        userRepository.save(newUser("dup@propnest.com", tenantRole, UserStatus.A));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(newUser("dup@propnest.com", tenantRole, UserStatus.A)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("findByRole_RoleNameIgnoreCase returns only users of that role")
    void findByRole() {
        userRepository.save(newUser("t1@propnest.com", tenantRole, UserStatus.A));
        userRepository.save(newUser("o1@propnest.com", ownerRole, UserStatus.A));

        List<User> tenants = userRepository.findByRole_RoleNameIgnoreCase("tenant");

        assertThat(tenants).hasSize(1);
        assertThat(tenants.get(0).getEmail()).isEqualTo("t1@propnest.com");
    }

    @Test
    @DisplayName("findByStatus returns only users with that status")
    void findByStatus() {
        userRepository.save(newUser("active@propnest.com", tenantRole, UserStatus.A));
        userRepository.save(newUser("susp@propnest.com", tenantRole, UserStatus.S));

        assertThat(userRepository.findByStatus(UserStatus.A)).hasSize(1);
        assertThat(userRepository.findByStatus(UserStatus.S)).hasSize(1);
        assertThat(userRepository.findByStatus(UserStatus.I)).isEmpty();
    }

    @Test
    @DisplayName("findByRole_RoleNameIgnoreCaseAndStatus combines both filters")
    void findByRoleAndStatus() {
        userRepository.save(newUser("ta@propnest.com", tenantRole, UserStatus.A));
        userRepository.save(newUser("ts@propnest.com", tenantRole, UserStatus.S));
        userRepository.save(newUser("oa@propnest.com", ownerRole, UserStatus.A));

        List<User> result =
                userRepository.findByRole_RoleNameIgnoreCaseAndStatus("TENANT", UserStatus.A);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ta@propnest.com");
    }

    @Test
    @DisplayName("status persists and reads back as the same enum value")
    void statusPersists() {
        User saved = userRepository.save(newUser("st@propnest.com", tenantRole, UserStatus.S));

        Optional<User> reloaded = userRepository.findById(saved.getUserId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(UserStatus.S);
    }

    @Test
    @DisplayName("the user keeps its associated role")
    void roleAssociationPersists() {
        User saved = userRepository.save(newUser("r@propnest.com", ownerRole, UserStatus.A));

        Optional<User> reloaded = userRepository.findById(saved.getUserId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getRole().getRoleName()).isEqualTo("OWNER");
    }
}
