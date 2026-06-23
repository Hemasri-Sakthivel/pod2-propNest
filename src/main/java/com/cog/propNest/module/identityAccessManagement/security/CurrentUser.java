package com.cog.propNest.module.identityAccessManagement.security;

/**
 * Identity of the authenticated caller, derived from a validated access token
 * and made available to controllers/services for the duration of the request.
 *
 * @param userId the authenticated user's id
 * @param email  the authenticated user's email
 * @param role   the authenticated user's role name (e.g. {@code REAL_ESTATE_ADMIN})
 */
public record CurrentUser(Long userId, String email, String role) {
}
