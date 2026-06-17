package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.exception.TokenMissingException;

/**
 * Holds the {@link CurrentUser} for the current request on a {@link ThreadLocal}.
 * Populated by {@code JwtAuthenticationFilter} and cleared once the request
 * completes.
 */
public final class AuthContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** @return the current user, or {@code null} if the request is unauthenticated. */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /**
     * @return the current user, never {@code null}.
     * @throws TokenMissingException if no authenticated user is bound to the request.
     */
    public static CurrentUser require() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new TokenMissingException();
        }
        return user;
    }
}
