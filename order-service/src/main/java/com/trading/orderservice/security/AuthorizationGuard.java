package com.trading.orderservice.security;

import org.springframework.stereotype.Component;

import com.trading.orderservice.exception.AccessDeniedException;

/**
 * Centralizes role-based authorization checks based on the {@code X-User-Role}
 * header propagated by the API gateway.
 */
@Component
public class AuthorizationGuard {

    /**
     * Ensures the caller has the {@code ADMIN} role.
     *
     * @param role the role header value. May be null.
     * @throws AccessDeniedException if the caller is not an admin.
     */
    public void requireAdmin(String role) {
        if (Role.from(role) != Role.ADMIN) {
            throw new AccessDeniedException("Admin privileges required");
        }
    }

    /**
     * Ensures the caller is either an admin or the owner of the resource.
     *
     * @param authenticatedUserId the authenticated user id (from the gateway). May be null.
     * @param role                the role header value. May be null.
     * @param resourceUserId      the owner id of the target resource. May be null.
     * @throws AccessDeniedException if the caller is neither an admin nor the resource owner.
     */
    public void requireOwnerOrAdmin(String authenticatedUserId, String role, String resourceUserId) {
        if (Role.from(role) == Role.ADMIN) {
            return;
        }
        if (authenticatedUserId == null || !authenticatedUserId.equals(resourceUserId)) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }
    }
}
