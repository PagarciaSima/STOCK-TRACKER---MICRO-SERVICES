package com.trading.portfolioservice.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Typed portfolio response with current P&L calculation.
 * Replaces {@code Map<String, Object>} in {@code PortfolioController}.
 *
 * @param userId          Identifier of the user. Cannot be null or blank.
 * @param holdings        List of holding items. Cannot be null.
 * @param totalInvested   Total amount invested. Cannot be null or negative.
 * @param currentValue    Current market value of all holdings. Cannot be null or negative.
 * @param totalPnL        Total profit and loss. Cannot be null.
 * @param totalPnLPercent Total P&L percentage. Cannot be null.
 * @param isProfit        Whether the portfolio is in profit.
 */
public record PortfolioResponse(
        String userId,
        List<HoldingItem> holdings,
        BigDecimal totalInvested,
        BigDecimal currentValue,
        BigDecimal totalPnL,
        BigDecimal totalPnLPercent,
        boolean isProfit) {
}
