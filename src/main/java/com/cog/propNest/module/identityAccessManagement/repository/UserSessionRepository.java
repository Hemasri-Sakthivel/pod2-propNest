package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);
}
