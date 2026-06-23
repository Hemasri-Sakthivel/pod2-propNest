package com.cog.propNest.module.identityAccessManagement.entity;

/**
 * Account status for a {@link User}.
 *
 * <ul>
 *   <li>{@code A} — Active: can log in and use the system.</li>
 *   <li>{@code I} — Inactive: soft-deleted; record retained so audit history is preserved.</li>
 *   <li>{@code S} — Suspended: cannot log in.</li>
 * </ul>
 */
public enum UserStatus {
    A,
    I,
    S
}
