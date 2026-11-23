package com.trading.orderservice.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.trading.orderservice.exception.AccessDeniedException;

/**
 * Unit tests for {@link AuthorizationGuard} role-based checks.
 */
class AuthorizationGuardTest {

    private final AuthorizationGuard guard = new AuthorizationGuard();

    @Test
    void shouldAllowAdminToAccessAnyResource() {
        assertThatCode(() -> guard.requireOwnerOrAdmin("user-1", "ADMIN", "user-2"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowOwnerToAccessOwnResource() {
        assertThatCode(() -> guard.requireOwnerOrAdmin("user-1", "USER", "user-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDenyUserAccessingAnotherUsersResource() {
        assertThatThrownBy(() -> guard.requireOwnerOrAdmin("user-1", "USER", "user-2"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldDenyNonAdminOnAdminEndpoint() {
        assertThatThrownBy(() -> guard.requireAdmin("USER"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowAdminOnAdminEndpoint() {
        assertThatCode(() -> guard.requireAdmin("ADMIN"))
                .doesNotThrowAnyException();
    }
}
