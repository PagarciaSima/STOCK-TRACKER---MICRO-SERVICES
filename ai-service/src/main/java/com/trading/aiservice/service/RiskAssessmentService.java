package com.trading.aiservice.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trading.aiservice.config.AiProperties;
import com.trading.aiservice.dto.AiAdvice;
import com.trading.aiservice.dto.Decision;
import com.trading.aiservice.dto.OrderRiskRequest;
import com.trading.aiservice.dto.RiskAssessment;
import com.trading.aiservice.dto.RiskLevel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the risk assessment: deterministic rules decide, and the LLM is
 * consulted (advisory only) when the rules return the REVIEW band.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {

    private final RiskRuleEngine ruleEngine;
    private final AiRiskAdvisor aiRiskAdvisor;
    private final AiProperties properties;

    /**
     * Assesses the risk of an order.
     *
     * @param request the order and behavioural features. Cannot be null.
     * @return the structured {@link RiskAssessment}. Never null.
     */
    public RiskAssessment assess(OrderRiskRequest request) {
        RuleOutcome outcome = ruleEngine.evaluate(request);
        Decision baseDecision = decisionFromScore(outcome.score());
        RiskLevel riskLevel = levelFromScore(outcome.score());

        AiAdvice ai = new AiAdvice(false, "none", null, null, "NONE", 0);
        if (baseDecision == Decision.REVIEW && properties.isConsultOnReview()) {
            ai = aiRiskAdvisor.advise(request, outcome);
        }

        // AI may escalate ALLOW -> REVIEW, but never approve a BLOCK nor block alone.
        Decision finalDecision = baseDecision;
        if (baseDecision == Decision.ALLOW && "REVIEW".equalsIgnoreCase(ai.suggestedSeverity())) {
            finalDecision = Decision.REVIEW;
        }

        List<String> reasons = outcome.hits().isEmpty()
                ? List.of("No risk rules triggered")
                : outcome.hits().stream()
                        .map(hit -> hit.rule() + ": " + hit.detail())
                        .collect(Collectors.toList());

        boolean requiresReview = finalDecision == Decision.REVIEW;
        log.info("Risk assessment user={} symbol={} score={} decision={} aiUsed={} provider={}",
                request.getUserId(), request.getSymbol(), outcome.score(), finalDecision,
                ai.used(), ai.provider());

        return new RiskAssessment(
                request.getUserId(),
                request.getSymbol(),
                finalDecision,
                riskLevel,
                outcome.score(),
                reasons,
                outcome.hits(),
                ai,
                requiresReview,
                Instant.now());
    
    }

    private Decision decisionFromScore(int score) {
        AiProperties.Risk cfg = properties.getRisk();
        if (score >= cfg.getHighScoreThreshold()) {
            return Decision.BLOCK;
        }
        if (score >= cfg.getMediumScoreThreshold()) {
            return Decision.REVIEW;
        }
        return Decision.ALLOW;
    }

    private RiskLevel levelFromScore(int score) {
        AiProperties.Risk cfg = properties.getRisk();
        if (score >= cfg.getHighScoreThreshold()) {
            return RiskLevel.HIGH;
        }
        if (score >= cfg.getMediumScoreThreshold()) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
