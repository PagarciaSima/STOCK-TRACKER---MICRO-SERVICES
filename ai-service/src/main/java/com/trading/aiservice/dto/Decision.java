package com.trading.aiservice.dto;

/**
 * Final risk decision for an order.
 */
public enum Decision {
    /** Order is allowed to proceed. */
    ALLOW,
    /** Order is allowed but flagged for manual review. */
    REVIEW,
    /** Order must be blocked. */
    BLOCK
}
