package com.trading.aiservice.service;

import java.util.List;

import com.trading.aiservice.dto.RuleHit;

/**
 * Result of evaluating the deterministic rule engine.
 *
 * @param score aggregated risk score (0-100).
 * @param hits  rule hits that contributed to the score.
 */
public record RuleOutcome(int score, List<RuleHit> hits) {
}
