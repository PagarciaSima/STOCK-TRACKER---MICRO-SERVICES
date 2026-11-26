package com.trading.aiservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.trading.aiservice.config.AiProperties;
import com.trading.aiservice.dto.OrderRiskRequest;
import com.trading.aiservice.dto.RuleHit;
import com.trading.aiservice.dto.RuleSeverity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Deterministic rule engine: computes a risk score and rule hits from order
 * features. This is the authoritative decision source; the LLM is advisory only.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RiskRuleEngine {

    private final AiProperties properties;

    /**
     * Evaluates all risk rules for the given order.
     *
     * @param request the order and its behavioural features. Cannot be null.
     * @return a {@link RuleOutcome} with the aggregated score (0-100) and hits.
     */
    public RuleOutcome evaluate(OrderRiskRequest request) {
        AiProperties.Risk cfg = properties.getRisk();
        List<RuleHit> hits = new ArrayList<>();
        int score = 0;

        if (request.isAccountBlocked()) {
            hits.add(new RuleHit("ACCOUNT_STATUS", RuleSeverity.HIGH, "Account is blocked or suspended"));
            score += 80;
        }
        if (!request.isSymbolAllowed()) {
            hits.add(new RuleHit("SYMBOL_NOT_ALLOWED", RuleSeverity.HIGH, "Symbol is not allowed for trading"));
            score += 80;
        }

        BigDecimal total = request.getTotalAmount();
        if (total != null && cfg.getMaxOrderAmount() != null
                && total.compareTo(cfg.getMaxOrderAmount()) > 0) {
            hits.add(new RuleHit("MAX_ORDER_AMOUNT", RuleSeverity.HIGH,
                    "Order amount " + total + " exceeds max " + cfg.getMaxOrderAmount()));
            score += 60;
        }

        BigDecimal balance = request.getWalletBalance();
        if (balance != null && balance.signum() > 0 && total != null) {
            if (total.compareTo(balance) > 0) {
                hits.add(new RuleHit("INSUFFICIENT_BALANCE", RuleSeverity.HIGH,
                        "Order amount exceeds wallet balance"));
                score += 60;
            } else if (cfg.getBalanceRatioThreshold() != null
                    && total.compareTo(balance.multiply(cfg.getBalanceRatioThreshold())) > 0) {
                hits.add(new RuleHit("BALANCE_RATIO", RuleSeverity.MEDIUM,
                        "Order consumes more than the configured share of the balance"));
                score += 25;
            }
        }

        if (request.getRecentOrderCount() > cfg.getVelocityMaxOrders()) {
            hits.add(new RuleHit("VELOCITY", RuleSeverity.HIGH,
                    request.getRecentOrderCount() + " orders within "
                            + cfg.getVelocityWindowMinutes() + " min"));
            score += 50;
        }
        if (request.isDuplicateRecentOrder()) {
            hits.add(new RuleHit("DUPLICATE_ORDER", RuleSeverity.MEDIUM, "An identical recent order exists"));
            score += 30;
        }
        if (request.isOppositeRecentOrder()) {
            hits.add(new RuleHit("WASH_TRADE", RuleSeverity.MEDIUM,
                    "Recent opposite-side order on the same symbol"));
            score += 25;
        }

        int clamped = Math.min(100, score);
        log.debug("Rule evaluation user={} symbol={} score={} hits={}",
                request.getUserId(), request.getSymbol(), clamped, hits.size());
        return new RuleOutcome(clamped, hits);
    }
}
