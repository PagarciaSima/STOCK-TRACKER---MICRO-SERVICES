package com.trading.aiservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trading.aiservice.config.AiProperties;
import com.trading.aiservice.dto.OrderRiskRequest;

/**
 * Unit tests for the deterministic {@link RiskRuleEngine}.
 */
class RiskRuleEngineTest {

    private RiskRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RiskRuleEngine(new AiProperties());
    }

    @Test
    void shouldReturnLowScoreForNormalOrder() {
        RuleOutcome outcome = engine.evaluate(sample());
        assertThat(outcome.score()).isLessThan(40);
        assertThat(outcome.hits()).isEmpty();
    }

    @Test
    void shouldFlagMaxAmountRule() {
        OrderRiskRequest req = sample();
        req.setTotalAmount(BigDecimal.valueOf(100000));
        RuleOutcome outcome = engine.evaluate(req);
        assertThat(outcome.score()).isGreaterThanOrEqualTo(60);
        assertThat(outcome.hits()).anyMatch(h -> h.rule().equals("MAX_ORDER_AMOUNT"));
    }

    @Test
    void shouldFlagVelocityRule() {
        OrderRiskRequest req = sample();
        req.setRecentOrderCount(30);
        RuleOutcome outcome = engine.evaluate(req);
        assertThat(outcome.hits()).anyMatch(h -> h.rule().equals("VELOCITY"));
    }

    @Test
    void shouldFlagBlockedAccountAsHigh() {
        OrderRiskRequest req = sample();
        req.setAccountBlocked(true);
        RuleOutcome outcome = engine.evaluate(req);
        assertThat(outcome.score()).isGreaterThanOrEqualTo(70);
    }

    private OrderRiskRequest sample() {
        OrderRiskRequest r = new OrderRiskRequest();
        r.setUserId("u1");
        r.setSymbol("AAPL");
        r.setType("BUY");
        r.setQuantity(10);
        r.setPrice(BigDecimal.valueOf(100));
        r.setTotalAmount(BigDecimal.valueOf(1000));
        r.setWalletBalance(BigDecimal.valueOf(10000));
        r.setRecentOrderCount(1);
        r.setSymbolAllowed(true);
        return r;
    }
}
