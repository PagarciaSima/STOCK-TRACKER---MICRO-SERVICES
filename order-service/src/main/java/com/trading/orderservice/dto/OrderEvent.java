package com.trading.orderservice.dto;

import java.math.BigDecimal;

/**
 * Typed Kafka event published when an order is executed or fails.
 * Replaces the generic {@code Map<String, Object>} used in
 * {@code OrderService#publishOrderEvent}.
 *
 * @param orderId     Unique identifier of the order. Cannot be null or blank.
 * @param userId      Identifier of the user who placed the order. Cannot be null or blank.
 * @param symbol      Stock ticker symbol. Cannot be null or blank.
 * @param type        Order type (BUY or SELL). Cannot be null.
 * @param quantity    Number of shares. Must be greater than or equal to 1.
 * @param price       Unit price at execution. Cannot be null or negative.
 * @param totalAmount Total amount (price * quantity). Cannot be null or negative.
 * @param status      Final status of the order (EXECUTED or FAILED). Cannot be null.
 * @param reason      Failure reason, if any (nullable).
 */
public record OrderEvent(
        String orderId,
        String userId,
        String symbol,
        String type,
        int quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        String status,
        String reason) {
}
