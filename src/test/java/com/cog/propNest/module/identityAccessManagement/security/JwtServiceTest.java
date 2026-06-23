package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.entity.Role;
import com.cog.propNest.module.identityAccessManagement.entity.User;
import com.cog.propNest.module.identityAccessManagement.entity.UserStatus;
import com.cog.propNest.module.identityAccessManagement.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService")
class JwtServiceTest {

    // Base64-encoded secrets, each >= 256 bits.
    private static final String SECRET =
            "Y2hhbmdlLXRoaXMtc2VjcmV0LWtleS1mb3ItcHJvcG5lc3QtaWFtLWp3dC1zaWduaW5nLTI1Ng==";
    private static final String OTHER_SECRET =
            "YW5vdGhlci1jb21wbGV0ZWx5LWRpZmZlcmVudC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLTI1Ng==";
    private static final String ISSUER = "propNest-IAM";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 1_800_000L, 604_800_000L, ISSUER);
        user = User.builder()
                .userId(101L)
                .role(new Role(6, "REAL_ESTATE_ADMIN"))
                .name("Admin")
                .email("admin@propnest.com")
                .passwordHash("HASH")
                .status(UserStatus.A)
                .build();
    }

    @Test
    @DisplayName("generated access token round-trips back to the same identity")
    void generateAndParse_roundTrip() {
        String token = jwtService.generateAccessToken(user);

        CurrentUser parsed = jwtService.parseAccessToken(token);

        assertThat(parsed.userId()).isEqualTo(101L);
        assertThat(parsed.email()).isEqualTo("admin@propnest.com");
        assertThat(parsed.role()).isEqualTo("REAL_ESTATE_ADMIN");
    }

    @Test
    @DisplayName("generated access token is a three-part JWT")
    void generatedToken_isJwt() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("parsing a malformed token throws InvalidTokenException")
    void parse_malformed() {
        assertThatThrownBy(() -> jwtService.parseAccessToken("not-a-real-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("parsing a token signed with a different key fails the signature check")
    void parse_badSignature() {
        JwtService other = new JwtService(OTHER_SECRET, 1_800_000L, 604_800_000L, ISSUER);
        String foreignToken = other.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseAccessToken(foreignToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("parsing an expired token throws InvalidTokenException")
    void parse_expired() {
        JwtService shortLived = new JwtService(SECRET, -1_000L, 604_800_000L, ISSUER);
        String expired = shortLived.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseAccessToken(expired))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("a token from a different issuer is rejected")
    void parse_wrongIssuer() {
        JwtService otherIssuer = new JwtService(SECRET, 1_800_000L, 604_800_000L, "someone-else");
        String token = otherIssuer.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseAccessToken(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("generateRefreshToken returns a non-blank, unique value each call")
    void generateRefreshToken_unique() {
        String a = jwtService.generateRefreshToken();
        String b = jwtService.generateRefreshToken();

        assertThat(a).isNotBlank();
        assertThat(b).isNotBlank();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("refreshTokenExpiry is in the future")
    void refreshTokenExpiry_future() {
        assertThat(jwtService.refreshTokenExpiry()).isAfter(Instant.now());
    }
}
