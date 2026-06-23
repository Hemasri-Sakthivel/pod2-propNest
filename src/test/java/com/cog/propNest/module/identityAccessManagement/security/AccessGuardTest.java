package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.exception.AccessDeniedException;
import com.cog.propNest.module.identityAccessManagement.exception.TokenMissingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccessGuard")
class AccessGuardTest {

    private final AccessGuard accessGuard = new AccessGuard();

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("requireAdmin returns the caller when they are REAL_ESTATE_ADMIN")
    void requireAdmin_allowsAdmin() {
        CurrentUser admin = new CurrentUser(1L, "admin@propnest.com", "REAL_ESTATE_ADMIN");
        AuthContext.set(admin);

        assertThat(accessGuard.requireAdmin()).isSameAs(admin);
    }

    @Test
    @DisplayName("requireAdmin throws AccessDeniedException for a non-admin role")
    void requireAdmin_deniesNonAdmin() {
        AuthContext.set(new CurrentUser(2L, "tenant@propnest.com", "TENANT"));

        assertThatThrownBy(accessGuard::requireAdmin)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("requireAdmin throws TokenMissingException when unauthenticated")
    void requireAdmin_throwsWhenUnauthenticated() {
        AuthContext.clear();

        assertThatThrownBy(accessGuard::requireAdmin)
                .isInstanceOf(TokenMissingException.class);
    }
}
