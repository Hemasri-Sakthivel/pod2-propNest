package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.dto.UserResponse;
import com.cog.propNest.module.identityAccessManagement.exception.IamExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.exception.IncorrectPasswordException;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.security.CurrentUser;
import com.cog.propNest.module.identityAccessManagement.service.UserSelfService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSelfController")
class UserSelfControllerTest {

    @Mock private UserSelfService userSelfService;
    @InjectMocks private UserSelfController userSelfController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userSelfController)
                .setControllerAdvice(new IamExceptionHandler(), new GlobalExceptionHandler())
                .build();
        AuthContext.set(new CurrentUser(101L, "hemasri@propnest.com", "TENANT"));
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("GET /IAM/users/me returns the caller's profile")
    void getProfile_ok() throws Exception {
        when(userSelfService.getProfile(101L)).thenReturn(
                new UserResponse(101L, "Hemasri S", "hemasri@propnest.com",
                        "9876543210", "TENANT", "A"));

        mockMvc.perform(get("/IAM/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.email").value("hemasri@propnest.com"))
                .andExpect(jsonPath("$.role").value("TENANT"))
                .andExpect(jsonPath("$.status").value("A"));
    }

    @Test
    @DisplayName("GET /IAM/users/me returns 401 when no token identity is bound")
    void getProfile_unauthenticated() throws Exception {
        AuthContext.clear();

        mockMvc.perform(get("/IAM/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /IAM/users/me updates the caller's profile")
    void updateProfile_ok() throws Exception {
        doNothing().when(userSelfService).updateProfile(eq(101L), any());

        mockMvc.perform(put("/IAM/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hemasri Sakthivel","phone":"9876540000"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));

        verify(userSelfService).updateProfile(eq(101L), any());
    }

    @Test
    @DisplayName("PUT /IAM/users/me returns 400 when name is blank")
    void updateProfile_validationFails() throws Exception {
        mockMvc.perform(put("/IAM/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","phone":"9876540000"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /IAM/users/me/password changes the caller's password")
    void changePassword_ok() throws Exception {
        doNothing().when(userSelfService).changePassword(eq(101L), any());

        mockMvc.perform(put("/IAM/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"Secret@123","newPassword":"NewPass@456"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @DisplayName("PUT /IAM/users/me/password returns 400 when the current password is wrong")
    void changePassword_incorrect() throws Exception {
        doThrow(new IncorrectPasswordException())
                .when(userSelfService).changePassword(eq(101L), any());

        mockMvc.perform(put("/IAM/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"wrong","newPassword":"NewPass@456"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }
}
