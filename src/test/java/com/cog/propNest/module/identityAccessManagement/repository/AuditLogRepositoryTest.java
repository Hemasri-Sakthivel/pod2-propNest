package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("AuditLogRepository")
class AuditLogRepositoryTest {

    @Autowired private AuditLogRepository auditLogRepository;

    private AuditLog log(long userId, String action, Instant ts) {
        return AuditLog.builder()
                .userId(userId).action(action).entityType("USER").timeStamp(ts).build();
    }

    @Test
    @DisplayName("save assigns an id and persists the log")
    void save_assignsId() {
        AuditLog saved = auditLogRepository.save(log(101L, "USER_LOGIN", Instant.now()));

        assertThat(saved.getAuditId()).isNotNull();
    }

    @Test
    @DisplayName("findByUserIdOrderByTimeStampDesc returns only that user's logs, newest first")
    void findByUserId_ordered() {
        Instant now = Instant.now();
        auditLogRepository.save(log(101L, "USER_LOGIN", now.minusSeconds(60)));
        auditLogRepository.save(log(101L, "PASSWORD_CHANGED", now));
        auditLogRepository.save(log(202L, "USER_LOGIN", now));

        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimeStampDesc(101L);

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getAction()).isEqualTo("PASSWORD_CHANGED");
        assertThat(logs.get(1).getAction()).isEqualTo("USER_LOGIN");
    }

    @Test
    @DisplayName("findByUserIdOrderByTimeStampDesc returns empty for a user with no logs")
    void findByUserId_empty() {
        assertThat(auditLogRepository.findByUserIdOrderByTimeStampDesc(999L)).isEmpty();
    }

    @Test
    @DisplayName("findAllByOrderByTimeStampDesc returns every log newest first")
    void findAll_ordered() {
        Instant now = Instant.now();
        auditLogRepository.save(log(101L, "USER_LOGIN", now.minusSeconds(120)));
        auditLogRepository.save(log(202L, "USER_CREATED", now));

        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimeStampDesc();

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getAction()).isEqualTo("USER_CREATED");
    }
}
