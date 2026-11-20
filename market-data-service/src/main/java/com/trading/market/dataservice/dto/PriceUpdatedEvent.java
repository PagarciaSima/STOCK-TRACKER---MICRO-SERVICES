package com.trading.market.dataservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Typed Kafka event published when a stock price is updated.
 * Replaces the generic {@code Map<String, Object>} previously used for the
 * {@code stock.price.updated} topic.
 *
 * @param symbol     Stock ticker symbol. Cannot be null or blank.
 * @param price      New unit price. Cannot be null or negative.
 * @param timestamp  Moment when the price was observed. Cannot be null.
 */
public record PriceUpdatedEvent(String symbol, BigDecimal price, LocalDateTime timestamp) {
}
