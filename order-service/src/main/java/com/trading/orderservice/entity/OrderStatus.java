package com.trading.orderservice.entity;

/**
 * Lifecycle status of an order.
 */
public enum OrderStatus {

    /** Order created but not yet executed. */
    PENDING,

    /** Order successfully executed. */
    EXECUTED,

    /** Order failed (e.g. insufficient funds). */
    FAILED
}
