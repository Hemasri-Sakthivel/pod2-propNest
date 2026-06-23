package com.cog.propNest.module.identityAccessManagement.service;

/**
 * Canonical action names written to the {@code audit_log}.
 */
public final class AuditActions {

    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USER_LOGOUT = "USER_LOGOUT";
    public static final String TOKEN_REFRESHED = "TOKEN_REFRESHED";
    public static final String PROFILE_UPDATED = "PROFILE_UPDATED";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_STATUS_UPDATED = "USER_STATUS_UPDATED";
    public static final String USER_ROLE_UPDATED = "USER_ROLE_UPDATED";

    public static final String ENTITY_USER = "USER";

    private AuditActions() {
    }
}
