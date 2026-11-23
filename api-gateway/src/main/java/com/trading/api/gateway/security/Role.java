package com.trading.api.gateway.security;

/**
 * Authorization role propagated in the JWT {@code role} claim.
 */
public enum Role {

    /** Full access: can manage any user, order and portfolio. */
    ADMIN,

    /** Standard user: can only access their own resources. */
    USER;

    /**
     * Parses a role value safely, defaulting to {@link #USER} for null, blank or
     * unknown values.
     *
     * @param value the raw role value (e.g. from a JWT claim). May be null.
     * @return the parsed {@link Role}; {@link #USER} when the value is invalid. Never null.
     */
    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}
