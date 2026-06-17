package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.dto.UserListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.exception.IamExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidUserStatusException;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.security.CurrentUser;
import com.cog.propNest.module.identityAccessManagement.service.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController")
class AdminUserControllerTest {

    @Mock private AdminUserService adminUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminUserController controller =
                new AdminUserController(adminUserService, new AccessGuard());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new IamExceptionHandler(), new GlobalExceptionHandler())
                .build();
        AuthContext.set(new CurrentUser(1L, "admin@propnest.com", "REAL_ESTATE_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    private UserResponse sampleUser() {
        return new UserResponse(101L, "Ravi Kumar", "ravi@propnest.com",
                "9123456789", "TENANT", "A");
    }

    @Test
    @DisplayName("POST /IAM/admin/users returns 201")
    void createUser_created() throws Exception {
        doNothing().when(adminUserService).createUser(any());

        mockMvc.perform(post("/IAM/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ravi Kumar","email":"ravi@propnest.com",
                                "phone":"9123456789","password":"Temp@1234","roleId":2}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    @DisplayName("POST /IAM/admin/users returns 403 for a non-admin caller")
    void createUser_forbidden() throws Exception {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        mockMvc.perform(post("/IAM/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ravi Kumar","email":"ravi@propnest.com",
                                "phone":"9123456789","password":"Temp@1234","roleId":2}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /IAM/admin/users returns 200 with the user list")
    void getAllUsers_ok() throws Exception {
        when(adminUserService.getAllUsers(null, null))
                .thenReturn(new UserListResponse(List.of(sampleUser())));

        mockMvc.perform(get("/IAM/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].userId").value(101))
                .andExpect(jsonPath("$.users[0].role").value("TENANT"));
    }

    @Test
    @DisplayName("GET /IAM/admin/users passes role and status filters to the service")
    void getAllUsers_withFilters() throws Exception {
        when(adminUserService.getAllUsers("TENANT", "A"))
                .thenReturn(new UserListResponse(List.of(sampleUser())));

        mockMvc.perform(get("/IAM/admin/users").param("role", "TENANT").param("status", "A"))
                .andExpect(status().isOk());

        verify(adminUserService).getAllUsers("TENANT", "A");
    }

    @Test
    @DisplayName("GET /IAM/admin/users returns 403 for a non-admin caller")
    void getAllUsers_forbidden() throws Exception {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        mockMvc.perform(get("/IAM/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /IAM/admin/users/{id} returns 200 with the user")
    void getUserById_ok() throws Exception {
        when(adminUserService.getUserById(101L)).thenReturn(sampleUser());

        mockMvc.perform(get("/IAM/admin/users/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ravi@propnest.com"));
    }

    @Test
    @DisplayName("GET /IAM/admin/users/{id} returns 404 when the user is missing")
    void getUserById_notFound() throws Exception {
        when(adminUserService.getUserById(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/IAM/admin/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /IAM/admin/users/{id}/status returns 200")
    void updateStatus_ok() throws Exception {
        doNothing().when(adminUserService).updateStatus(eq(101L), any());

        mockMvc.perform(put("/IAM/admin/users/101/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"S"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User status updated successfully"));
    }

    @Test
    @DisplayName("PUT /IAM/admin/users/{id}/status returns 400 for an invalid value")
    void updateStatus_invalid() throws Exception {
        doThrow(new InvalidUserStatusException("X"))
                .when(adminUserService).updateStatus(eq(101L), any());

        mockMvc.perform(put("/IAM/admin/users/101/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"X"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /IAM/admin/users/{id}/role returns 200")
    void updateRole_ok() throws Exception {
        doNothing().when(adminUserService).updateRole(eq(101L), any());

        mockMvc.perform(put("/IAM/admin/users/101/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":3}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated successfully"));
    }

    @Test
    @DisplayName("PUT /IAM/admin/users/{id}/role returns 403 for a non-admin caller")
    void updateRole_forbidden() throws Exception {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        mockMvc.perform(put("/IAM/admin/users/101/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleId":3}"""))
                .andExpect(status().isForbidden());
    }
}
