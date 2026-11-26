package com.trading.aiservice.dto;

/**
 * Advisory output from the LLM layer (never the sole decision maker).
 *
 * @param used             whether the LLM was consulted.
 * @param provider         provider that answered (ollama, gemini, none).
 * @param model            model name used, if any (nullable).
 * @param advisory         short natural-language reasoning (nullable).
 * @param suggestedSeverity "NONE" or "REVIEW" (advisory escalation).
 * @param latencyMs        provider latency in milliseconds.
 */
public record AiAdvice(
        boolean used,
        String provider,
        String model,
        String advisory,
        String suggestedSeverity,
        long latencyMs) {
}
