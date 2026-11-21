package com.trading.market.dataservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Current market price snapshot for a stock, including the daily range and
 * change metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {

    /** Stock ticker symbol. */
    private String symbol;

    /** Display name of the company. */
    private String companyName;

    /** Latest traded price. */
    private BigDecimal price;

    /** Absolute change versus the opening price. */
    private BigDecimal change;

    /** Percentage change versus the opening price. */
    private BigDecimal changePercent;

    /** Highest price observed during the session. */
    private BigDecimal high;

    /** Lowest price observed during the session. */
    private BigDecimal low;

    /** Opening price of the session. */
    private BigDecimal open;

    /** Traded volume. */
    private long volume;

    /** Timestamp of the last price update. */
    private LocalDateTime timestamp;

}
