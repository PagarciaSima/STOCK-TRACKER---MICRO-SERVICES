package com.trading.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Typed response for a stock price quote from market-data-service.
 * Replaces the generic {@code Map<String, Object>} previously returned by
 * {@code MarketDataClient}.
 *
 * @param symbol    Stock ticker symbol (e.g. "AAPL"). Cannot be null or blank.
 * @param price     Current unit price of the stock. Cannot be null or negative.
 * @param timestamp Moment when the price was observed. Cannot be null.
 */
public record StockPriceResponse(String symbol, BigDecimal price, LocalDateTime timestamp) {
}
