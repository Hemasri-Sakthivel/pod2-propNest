package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.dto.AccessTokenResponse;
import com.cog.propNest.module.identityAccessManagement.dto.LoginResponse;
import com.cog.propNest.module.identityAccessManagement.exception.EmailAlreadyExistsException;
import com.cog.propNest.module.identityAccessManagement.exception.IamExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidCredentialsException;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidRefreshTokenException;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.security.CurrentUser;
import com.cog.propNest.module.identityAccessManagement.service.AuthService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new IamExceptionHandler(), new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("POST /IAM/auth/register returns 201 with a success message")
    void register_created() throws Exception {
        doNothing().when(authService).register(any());

        mockMvc.perform(post("/IAM/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Admin","email":"admin@propnest.com",
                                "phone":"9876500000","password":"Admin@123","roleId":6}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(authService).register(any());
    }

    @Test
    @DisplayName("POST /IAM/auth/register returns 400 on invalid input")
    void register_validationFails() throws Exception {
        mockMvc.perform(post("/IAM/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","password":"x","roleId":null}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /IAM/auth/register returns 409 when the email already exists")
    void register_conflict() throws Exception {
        doThrow(new EmailAlreadyExistsException("admin@propnest.com"))
                .when(authService).register(any());

        mockMvc.perform(post("/IAM/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Admin","email":"admin@propnest.com",
                                "phone":"9876500000","password":"Admin@123","roleId":6}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /IAM/auth/login returns 200 with tokens")
    void login_ok() throws Exception {
        when(authService.login(any()))
                .thenReturn(new LoginResponse("access", "refresh", 101L, "TENANT"));

        mockMvc.perform(post("/IAM/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"hemasri@propnest.com","password":"Secret@123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    @DisplayName("POST /IAM/auth/login returns 401 on bad credentials")
    void login_unauthorized() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/IAM/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"hemasri@propnest.com","password":"wrong"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /IAM/auth/logout returns 200 and uses the token identity")
    void logout_ok() throws Exception {
        AuthContext.set(new CurrentUser(101L, "hemasri@propnest.com", "TENANT"));
        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/IAM/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout(101L, "refresh");
    }

    @Test
    @DisplayName("POST /IAM/auth/refresh-token returns 200 with a new access token")
    void refresh_ok() throws Exception {
        when(authService.refreshToken("refresh"))
                .thenReturn(new AccessTokenResponse("new-access"));

        mockMvc.perform(post("/IAM/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    @DisplayName("POST /IAM/auth/refresh-token returns 401 for a revoked token")
    void refresh_unauthorized() throws Exception {
        when(authService.refreshToken(any())).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/IAM/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"revoked"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /IAM/auth/refresh-token returns 400 when refreshToken is blank")
    void refresh_validationFails() throws Exception {
        mockMvc.perform(post("/IAM/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":""}"""))
                .andExpect(status().isBadRequest());
    }
}
