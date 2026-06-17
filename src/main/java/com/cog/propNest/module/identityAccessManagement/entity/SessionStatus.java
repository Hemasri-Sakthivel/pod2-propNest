package com.cog.propNest.module.identityAccessManagement.entity;

/**
 * Lifecycle status of a refresh-token {@link UserSession}.
 *
 * <ul>
 *   <li>{@code ACTIVE} — refresh token may still be used to mint access tokens.</li>
 *   <li>{@code REVOKED} — token was logged out or rotated and can no longer be used.</li>
 * </ul>
 */
public enum SessionStatus {
    ACTIVE,
    REVOKED
}
