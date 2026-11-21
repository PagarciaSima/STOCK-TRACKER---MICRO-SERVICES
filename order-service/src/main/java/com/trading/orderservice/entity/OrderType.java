package com.trading.orderservice.entity;

/**
 * Type of a stock order.
 */
public enum OrderType {

    /** Buy order: deducts funds from the user's wallet. */
    BUY,

    /** Sell order: credits funds to the user's wallet. */
    SELL
}
