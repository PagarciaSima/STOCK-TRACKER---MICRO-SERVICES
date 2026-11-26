package com.trading.orderservice.dto;

import java.util.List;

/**
 * Risk assessment response returned by the ai-service.
 *
 * @param userId              the user id.
 * @param symbol              the stock symbol.
 * @param decision            ALLOW | REVIEW | BLOCK.
 * @param riskLevel           LOW | MEDIUM | HIGH.
 * @param riskScore           aggregated risk score (0-100).
 * @param reasons             human-readable reasons.
 * @param requiresManualReview whether a human should review the order.
 */
public record RiskAssessmentResponse(
        String userId,
        String symbol,
        String decision,
        String riskLevel,
        int riskScore,
        List<String> reasons,
        boolean requiresManualReview) {
}
