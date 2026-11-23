package com.trading.api.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Role#from(String)} parsing and safe defaults.
 */
class RoleTest {

    @Test
    void shouldParseAdminCaseInsensitive() {
        assertThat(Role.from("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.from("admin")).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldParseUser() {
        assertThat(Role.from("USER")).isEqualTo(Role.USER);
    }

    @Test
    void shouldDefaultToUserWhenNullOrBlank() {
        assertThat(Role.from(null)).isEqualTo(Role.USER);
        assertThat(Role.from("   ")).isEqualTo(Role.USER);
    }

    @Test
    void shouldDefaultToUserWhenUnknownValue() {
        assertThat(Role.from("SUPERADMIN")).isEqualTo(Role.USER);
    }
}
