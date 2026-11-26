package com.trading.aiservice.dto;

/**
 * A single risk rule that was triggered for an order.
 *
 * @param rule     the rule code (e.g. VELOCITY). Cannot be null.
 * @param severity the severity of the hit. Cannot be null.
 * @param detail   human-readable explanation. Cannot be null.
 */
public record RuleHit(String rule, RuleSeverity severity, String detail) {
}
