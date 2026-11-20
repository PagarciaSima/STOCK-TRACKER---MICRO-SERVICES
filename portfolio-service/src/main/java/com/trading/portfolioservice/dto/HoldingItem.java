package com.trading.portfolioservice.dto;

import java.math.BigDecimal;

/**
 * A single holding item within a portfolio response.
 *
 * @param symbol           Stock ticker symbol. Cannot be null or blank.
 * @param quantity         Number of shares held. Must be greater than or equal to 1.
 * @param averageBuyPrice  Average buy price per share. Cannot be null or negative.
 * @param currentPrice     Current market price per share. Cannot be null or negative.
 * @param currentValue     Current market value (quantity * currentPrice). Cannot be null or negative.
 * @param invested         Total amount invested in this holding. Cannot be null or negative.
 * @param pnl              Profit and loss for this holding. Cannot be null.
 * @param pnlPercent       P&L percentage. Cannot be null.
 * @param isProfit         Whether this holding is in profit.
 */
public record HoldingItem(
        String symbol,
        int quantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal invested,
        BigDecimal pnl,
        BigDecimal pnlPercent,
        boolean isProfit) {
}
