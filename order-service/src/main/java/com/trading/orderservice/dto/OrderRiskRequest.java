package com.trading.orderservice.dto;

import java.math.BigDecimal;

/**
 * Risk assessment request sent to the ai-service (mirrors its contract).
 */
public record OrderRiskRequest(
        String userId,
        String symbol,
        String type,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        BigDecimal walletBalance,
        int recentOrderCount,
        boolean duplicateRecentOrder,
        boolean oppositeRecentOrder,
        int dailyOrderCount,
        BigDecimal dailyTurnover,
        boolean symbolAllowed,
        boolean accountBlocked) {
}
