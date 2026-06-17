package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.dto.RoleListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.RoleResponse;
import com.cog.propNest.module.identityAccessManagement.exception.IamExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.security.CurrentUser;
import com.cog.propNest.module.identityAccessManagement.service.RoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleController")
class RoleControllerTest {

    @Mock private RoleService roleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RoleController controller = new RoleController(roleService, new AccessGuard());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new IamExceptionHandler(), new GlobalExceptionHandler())
                .build();
        AuthContext.set(new CurrentUser(1L, "admin@propnest.com", "REAL_ESTATE_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("GET /IAM/admin/roles returns 200 with all roles")
    void getAllRoles_ok() throws Exception {
        when(roleService.getAllRoles()).thenReturn(new RoleListResponse(List.of(
                new RoleResponse(1, "OWNER"),
                new RoleResponse(6, "REAL_ESTATE_ADMIN"))));

        mockMvc.perform(get("/IAM/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0].roleId").value(1))
                .andExpect(jsonPath("$.roles[1].roleName").value("REAL_ESTATE_ADMIN"));
    }

    @Test
    @DisplayName("GET /IAM/admin/roles returns 403 for a non-admin caller")
    void getAllRoles_forbidden() throws Exception {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        mockMvc.perform(get("/IAM/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /IAM/admin/roles returns 401 when unauthenticated")
    void getAllRoles_unauthenticated() throws Exception {
        AuthContext.clear();

        mockMvc.perform(get("/IAM/admin/roles"))
                .andExpect(status().isUnauthorized());
    }
}
