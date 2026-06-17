package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.dto.AuditLogListResponse;
import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;
import com.cog.propNest.module.identityAccessManagement.exception.UserNotFoundException;
import com.cog.propNest.module.identityAccessManagement.repository.AuditLogRepository;
import com.cog.propNest.module.identityAccessManagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService")
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuditLogService auditLogService;

    private AuditLog log(long id, long userId) {
        return AuditLog.builder()
                .auditId(id).userId(userId).action(AuditActions.USER_LOGIN)
                .entityType(AuditActions.ENTITY_USER).timeStamp(Instant.now()).build();
    }

    @Test
    @DisplayName("getAllLogs returns all logs mapped to responses")
    void getAllLogs_success() {
        when(auditLogRepository.findAllByOrderByTimeStampDesc())
                .thenReturn(List.of(log(2, 101), log(1, 102)));

        AuditLogListResponse response = auditLogService.getAllLogs();

        assertThat(response.logs()).hasSize(2);
        assertThat(response.logs().get(0).auditId()).isEqualTo(2);
    }

    @Test
    @DisplayName("getAllLogs returns an empty list when there are no logs")
    void getAllLogs_empty() {
        when(auditLogRepository.findAllByOrderByTimeStampDesc()).thenReturn(List.of());

        assertThat(auditLogService.getAllLogs().logs()).isEmpty();
    }

    @Test
    @DisplayName("getLogsForUser returns that user's logs when the user exists")
    void getLogsForUser_success() {
        when(userRepository.existsById(101L)).thenReturn(true);
        when(auditLogRepository.findByUserIdOrderByTimeStampDesc(101L))
                .thenReturn(List.of(log(3, 101)));

        AuditLogListResponse response = auditLogService.getLogsForUser(101L);

        assertThat(response.logs()).hasSize(1);
        assertThat(response.logs().get(0).userId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("getLogsForUser throws UserNotFoundException for an unknown user")
    void getLogsForUser_userMissing() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> auditLogService.getLogsForUser(999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(auditLogRepository, never()).findByUserIdOrderByTimeStampDesc(999L);
    }
}
