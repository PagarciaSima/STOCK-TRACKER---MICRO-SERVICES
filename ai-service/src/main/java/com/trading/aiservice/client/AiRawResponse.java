package com.trading.aiservice.client;

/**
 * Raw response from an AI provider.
 *
 * @param provider the provider name (ollama, gemini).
 * @param model    the model used.
 * @param text     the raw text returned by the model.
 * @param latencyMs provider latency in milliseconds.
 */
public record AiRawResponse(String provider, String model, String text, long latencyMs) {
}
