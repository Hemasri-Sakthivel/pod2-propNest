package com.cog.propNest.module.identityAccessManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.dto.AuditLogListResponse;
import com.cog.propNest.module.identityAccessManagement.dto.AuditLogResponse;
import com.cog.propNest.module.identityAccessManagement.exception.IamExceptionHandler;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.security.AccessGuard;
import com.cog.propNest.module.identityAccessManagement.security.AuthContext;
import com.cog.propNest.module.identityAccessManagement.security.CurrentUser;
import com.cog.propNest.module.identityAccessManagement.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogController")
class AuditLogControllerTest {

    @Mock private AuditLogService auditLogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuditLogController controller = new AuditLogController(auditLogService, new AccessGuard());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new IamExceptionHandler(), new GlobalExceptionHandler())
                .build();
        AuthContext.set(new CurrentUser(1L, "admin@propnest.com", "REAL_ESTATE_ADMIN"));
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    private AuditLogResponse log() {
        return new AuditLogResponse(1L, 101L, "USER_LOGIN", "USER", Instant.now());
    }

    @Test
    @DisplayName("GET /IAM/admin/audit-log returns 200 with all logs")
    void getAllLogs_ok() throws Exception {
        when(auditLogService.getAllLogs())
                .thenReturn(new AuditLogListResponse(List.of(log())));

        mockMvc.perform(get("/IAM/admin/audit-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs[0].action").value("USER_LOGIN"));
    }

    @Test
    @DisplayName("GET /IAM/admin/audit-log returns 403 for a non-admin caller")
    void getAllLogs_forbidden() throws Exception {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        mockMvc.perform(get("/IAM/admin/audit-log"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /IAM/admin/audit-log/user/{id} returns 200 with that user's logs")
    void getLogsForUser_ok() throws Exception {
        when(auditLogService.getLogsForUser(101L))
                .thenReturn(new AuditLogListResponse(List.of(log())));

        mockMvc.perform(get("/IAM/admin/audit-log/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs[0].userId").value(101));
    }

    @Test
    @DisplayName("GET /IAM/admin/audit-log/user/{id} returns 404 for an unknown user")
    void getLogsForUser_notFound() throws Exception {
        when(auditLogService.getLogsForUser(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/IAM/admin/audit-log/user/999"))
                .andExpect(status().isNotFound());
    }
}
