package com.trading.user.service.entity;

/**
 * Authorization role of a user.
 */
public enum Role {

    /** Full access: can manage any user, order and portfolio. */
    ADMIN,

    /** Standard user: can only access their own resources. */
    USER
}
