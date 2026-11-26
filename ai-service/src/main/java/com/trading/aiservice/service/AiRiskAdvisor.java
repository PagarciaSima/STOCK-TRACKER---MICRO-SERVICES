package com.trading.aiservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.aiservice.client.AiProvider;
import com.trading.aiservice.client.AiRawResponse;
import com.trading.aiservice.config.AiProperties;
import com.trading.aiservice.dto.AiAdvice;
import com.trading.aiservice.dto.OrderRiskRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Consults an LLM (Ollama first, Gemini fallback) for an advisory opinion on a
 * gray-zone order. The LLM can only suggest REVIEW; it never makes the final
 * decision and never blocks on its own.
 */
@Component
@Slf4j
public class AiRiskAdvisor {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, AiProvider> providers;

    /**
     * Creates the advisor.
     *
     * @param properties   the ai properties. Cannot be null.
     * @param objectMapper the JSON mapper. Cannot be null.
     * @param providers    all available AI providers. Cannot be null.
     */
    public AiRiskAdvisor(AiProperties properties, ObjectMapper objectMapper, List<AiProvider> providers) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.providers = new HashMap<>();
        providers.forEach(p -> this.providers.put(p.name(), p));
    }

    /**
     * Asks the configured LLM for an advisory opinion.
     *
     * @param request the order features. Cannot be null.
     * @param outcome the rule engine outcome. Cannot be null.
     * @return an {@link AiAdvice}; never null (returns an "unavailable" advice on failure).
     */
    public AiAdvice advise(OrderRiskRequest request, RuleOutcome outcome) {
        Optional<AiRawResponse> raw = callProviders(buildPrompt(request, outcome));
        if (raw.isEmpty()) {
            return new AiAdvice(false, "none", null, "AI unavailable; rules only", "NONE", 0);
        }
        AiRawResponse r = raw.get();
        String severity = "NONE";
        String reason = r.text();
        try {
            JsonNode node = objectMapper.readTree(extractJson(r.text()));
            if (node.hasNonNull("severity")) {
                severity = node.get("severity").asText();
            }
            if (node.hasNonNull("reason")) {
                reason = node.get("reason").asText();
            }
        } catch (Exception e) {
            log.debug("Could not parse AI JSON, using raw text: {}", e.getMessage());
        }
        if (!"REVIEW".equalsIgnoreCase(severity)) {
            severity = "NONE";
        }
        return new AiAdvice(true, r.provider(), r.model(), reason, severity.toUpperCase(), r.latencyMs());
    }

    private Optional<AiRawResponse> callProviders(String prompt) {
        String strategy = properties.getProvider() == null ? "auto" : properties.getProvider().trim().toLowerCase();
        List<String> order = switch (strategy) {
            case "ollama" -> List.of("ollama");
            case "gemini" -> List.of("gemini");
            default -> List.of("ollama", "gemini");
        };
        for (String name : order) {
            AiProvider provider = providers.get(name);
            if (provider != null && provider.isAvailable()) {
                Optional<AiRawResponse> result = provider.generate(prompt);
                if (result.isPresent()) {
                    return result;
                }
                log.debug("Provider {} failed or unavailable, trying next", name);
            }
        }
        return Optional.empty();
    }

    private String buildPrompt(OrderRiskRequest request, RuleOutcome outcome) {
        return "You are a trading risk analyst. Analyze the order features and decide whether a human should review it.\n"
                + "Respond ONLY with JSON: {\"severity\":\"NONE|REVIEW\",\"reason\":\"short explanation\"}.\n"
                + "Order: userId=" + request.getUserId() + ", symbol=" + request.getSymbol()
                + ", type=" + request.getType() + ", quantity=" + request.getQuantity()
                + ", price=" + request.getPrice() + ", total=" + request.getTotalAmount() + ".\n"
                + "Features: walletBalance=" + request.getWalletBalance()
                + ", recentOrderCount=" + request.getRecentOrderCount()
                + ", duplicateRecentOrder=" + request.isDuplicateRecentOrder()
                + ", oppositeRecentOrder=" + request.isOppositeRecentOrder()
                + ", dailyOrderCount=" + request.getDailyOrderCount()
                + ", dailyTurnover=" + request.getDailyTurnover()
                + ", symbolAllowed=" + request.isSymbolAllowed()
                + ", accountBlocked=" + request.isAccountBlocked() + ".\n"
                + "Rule score=" + outcome.score() + " hits=" + outcome.hits() + ".";
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : text;
    }
}
