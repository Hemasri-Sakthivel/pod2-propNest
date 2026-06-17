package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleNameIgnoreCase(String roleName);
}
