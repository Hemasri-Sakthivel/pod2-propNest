package com.cog.propNest.module.identityAccessManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A refresh-token session created at login. Access tokens are stateless (JWT)
 * and are not stored; only the long-lived refresh token is persisted so it can
 * be rotated and revoked.
 */
@Entity
@Table(name = "user_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionId")
    private Long sessionId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "refreshToken", nullable = false, length = 512)
    private String refreshToken;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "refreshTokenExpiryTime", nullable = false)
    private Instant refreshTokenExpiryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;
}
