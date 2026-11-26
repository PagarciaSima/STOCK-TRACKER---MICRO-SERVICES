package com.trading.aiservice.client;

import java.util.Optional;

/**
 * Abstraction for an LLM provider (Ollama, Gemini).
 */
public interface AiProvider {

    /** @return the provider name. */
    String name();

    /** @return whether the provider is enabled and configured. */
    boolean isAvailable();

    /**
     * Sends a prompt to the provider.
     *
     * @param prompt the prompt to send. Cannot be null.
     * @return an {@link Optional} with the raw response, or empty on failure/unavailable.
     */
    Optional<AiRawResponse> generate(String prompt);
}
