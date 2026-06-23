package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.SessionStatus;
import com.cog.propNest.module.identityAccessManagement.entity.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserSessionRepository")
class UserSessionRepositoryTest {

    @Autowired private UserSessionRepository userSessionRepository;

    private UserSession session(String token, SessionStatus status) {
        return UserSession.builder()
                .userId(101L).refreshToken(token)
                .createdAt(Instant.now())
                .refreshTokenExpiryTime(Instant.now().plusSeconds(3600))
                .status(status).build();
    }

    @Test
    @DisplayName("save assigns an id and persists the session")
    void save_assignsId() {
        UserSession saved = userSessionRepository.save(session("tok-1", SessionStatus.ACTIVE));

        assertThat(saved.getSessionId()).isNotNull();
    }

    @Test
    @DisplayName("findByRefreshToken returns the matching session")
    void findByRefreshToken_found() {
        userSessionRepository.save(session("tok-find", SessionStatus.ACTIVE));

        Optional<UserSession> found = userSessionRepository.findByRefreshToken("tok-find");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(101L);
        assertThat(found.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("findByRefreshToken returns empty for an unknown token")
    void findByRefreshToken_unknown() {
        assertThat(userSessionRepository.findByRefreshToken("ghost")).isEmpty();
    }

    @Test
    @DisplayName("a revoked status persists and reads back")
    void revokedStatusPersists() {
        userSessionRepository.save(session("tok-rev", SessionStatus.REVOKED));

        Optional<UserSession> found = userSessionRepository.findByRefreshToken("tok-rev");

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(SessionStatus.REVOKED);
    }
}
