package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Mints and validates stateless JWT access tokens, and generates opaque random
 * refresh tokens (which are persisted and validated against the database).
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;
    private final String issuer;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(
            @Value("${propnest.jwt.secret}") String secret,
            @Value("${propnest.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${propnest.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs,
            @Value("${propnest.jwt.issuer}") String issuer) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
        this.issuer = issuer;
    }

    /**
     * Build a signed access token whose subject is the userId and which carries
     * the user's email and role as claims.
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getRoleName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parse and validate an access token, returning the caller identity.
     *
     * @throws InvalidTokenException if the token is expired, malformed or has a
     *                               bad signature.
     */
    public CurrentUser parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            return new CurrentUser(userId, email, role);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Token missing or expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid access token");
        }
    }

    /** @return a fresh, cryptographically-random, URL-safe refresh token. */
    public String generateRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plusMillis(refreshTokenExpiryMs);
    }
}
