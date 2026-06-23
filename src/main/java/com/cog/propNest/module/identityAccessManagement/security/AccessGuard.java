package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Centralised role checks for protected endpoints. Authentication (a valid
 * token) is enforced by the filter; this guard enforces authorization (role).
 */
@Component
public class AccessGuard {

    /**
     * @return the authenticated caller after verifying they hold the
     * REAL_ESTATE_ADMIN role.
     * @throws AccessDeniedException if the caller is not an admin.
     */
    public CurrentUser requireAdmin() {
        CurrentUser user = AuthContext.require();
        if (!RoleNames.REAL_ESTATE_ADMIN.equals(user.role())) {
            throw new AccessDeniedException("Access denied: REAL_ESTATE_ADMIN role required");
        }
        return user;
    }
}
