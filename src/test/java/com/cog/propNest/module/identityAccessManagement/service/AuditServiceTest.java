package com.cog.propNest.module.identityAccessManagement.service;

import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;
import com.cog.propNest.module.identityAccessManagement.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService")
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private AuditService auditService;

    @Test
    @DisplayName("record persists an audit entry with userId, action and USER entity type")
    void record_persistsEntry() {
        auditService.record(101L, AuditActions.USER_LOGIN);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertThat(log.getUserId()).isEqualTo(101L);
        assertThat(log.getAction()).isEqualTo(AuditActions.USER_LOGIN);
        assertThat(log.getEntityType()).isEqualTo(AuditActions.ENTITY_USER);
    }

    @Test
    @DisplayName("record stamps the entry with a timestamp")
    void record_stampsTimestamp() {
        auditService.record(7L, AuditActions.PASSWORD_CHANGED);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTimeStamp()).isNotNull();
    }
}
