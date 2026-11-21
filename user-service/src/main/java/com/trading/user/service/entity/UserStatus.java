package com.trading.user.service.entity;

/**
 * Lifecycle status of a user account.
 */
public enum UserStatus {

    /** Account is active and can trade. */
    ACTIVE,

    /** Account is temporarily suspended. */
    SUSPENDED,

    /** Account is permanently blocked. */
    BLOCKED
}
