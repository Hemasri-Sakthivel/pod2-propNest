package com.cog.propNest.module.identityAccessManagement.config;

import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds the six fixed PropNest roles on startup if they are not already
 * present. roleId values are fixed by the API contract.
 */
@Configuration
public class RoleSeeder {

    private static final List<Role> DEFAULT_ROLES = List.of(
            new Role(1, "OWNER"),
            new Role(2, "TENANT"),
            new Role(3, "PROPERTY_MANAGER"),
            new Role(4, "TECHNICIAN"),
            new Role(5, "FINANCE_EXECUTIVE"),
            new Role(6, "REAL_ESTATE_ADMIN")
    );

    @Bean
    ApplicationRunner seedRoles(RoleRepository roleRepository) {
        return (ApplicationArguments args) -> {
            for (Role role : DEFAULT_ROLES) {
                if (!roleRepository.existsById(role.getRoleId())) {
                    roleRepository.save(role);
                }
            }
        };
    }
}
