package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.RoleListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RoleResponse;
import com.cog.propNest.module.identityAccessManagement.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read access to the fixed set of PropNest roles.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public RoleListResponse getAllRoles() {
        List<RoleResponse> roles = roleRepository.findAll(Sort.by("roleId")).stream()
                .map(RoleResponse::from)
                .toList();
        return new RoleListResponse(roles);
    }
}
