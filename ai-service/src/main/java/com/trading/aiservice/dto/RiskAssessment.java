package com.trading.aiservice.dto;

import java.time.Instant;
import java.util.List;

/**
 * Structured risk assessment returned for an order.
 *
 * @param userId              identifier of the user.
 * @param symbol              stock ticker symbol.
 * @param decision            authoritative decision (rules first, AI may escalate to REVIEW).
 * @param riskLevel           aggregated risk level.
 * @param riskScore           aggregated risk score (0-100).
 * @param reasons             human-readable reasons.
 * @param ruleHits            individual rule hits.
 * @param ai                  LLM advisory details.
 * @param requiresManualReview whether a human should review the order.
 * @param evaluatedAt         evaluation timestamp.
 */
public record RiskAssessment(
        String userId,
        String symbol,
        Decision decision,
        RiskLevel riskLevel,
        int riskScore,
        List<String> reasons,
        List<RuleHit> ruleHits,
        AiAdvice ai,
        boolean requiresManualReview,
        Instant evaluatedAt) {
}
