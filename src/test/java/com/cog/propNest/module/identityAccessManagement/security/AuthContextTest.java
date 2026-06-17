package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.exception.TokenMissingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthContext")
class AuthContextTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("get returns null when nothing is bound")
    void get_nullByDefault() {
        AuthContext.clear();
        assertThat(AuthContext.get()).isNull();
    }

    @Test
    @DisplayName("set then get returns the bound user")
    void setAndGet() {
        CurrentUser user = new CurrentUser(1L, "a@b.com", "TENANT");
        AuthContext.set(user);

        assertThat(AuthContext.get()).isSameAs(user);
    }

    @Test
    @DisplayName("require returns the bound user")
    void require_returnsUser() {
        CurrentUser user = new CurrentUser(1L, "a@b.com", "TENANT");
        AuthContext.set(user);

        assertThat(AuthContext.require()).isSameAs(user);
    }

    @Test
    @DisplayName("require throws TokenMissingException when nothing is bound")
    void require_throwsWhenEmpty() {
        AuthContext.clear();

        assertThatThrownBy(AuthContext::require)
                .isInstanceOf(TokenMissingException.class);
    }

    @Test
    @DisplayName("clear removes the bound user")
    void clear_removesUser() {
        AuthContext.set(new CurrentUser(1L, "a@b.com", "TENANT"));
        AuthContext.clear();

        assertThat(AuthContext.get()).isNull();
    }
}
