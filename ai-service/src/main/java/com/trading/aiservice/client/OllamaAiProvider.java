package com.trading.aiservice.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.trading.aiservice.config.AiProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * LLM provider backed by a local Ollama server ({@code /api/generate}).
 */
@Component
@Slf4j
public class OllamaAiProvider implements AiProvider {

    private final AiProperties.Ollama cfg;
    private final RestClient restClient;

    /**
     * Creates the provider with connect/read timeouts from configuration.
     *
     * @param properties the ai properties. Cannot be null.
     */
    public OllamaAiProvider(AiProperties properties) {
        this.cfg = properties.getOllama();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(cfg.getTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(cfg.getTimeoutMs()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String name() {
        return "ollama";
    }

    @Override
    public boolean isAvailable() {
        return cfg.isEnabled() && cfg.getBaseUrl() != null && !cfg.getBaseUrl().isBlank();
    }

    @Override
    public Optional<AiRawResponse> generate(String prompt) {
        if (!isAvailable()) {
            return Optional.empty();
        }
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", cfg.getModel());
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("format", "json");

            JsonNode response = restClient.post()
                    .uri(cfg.getBaseUrl() + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.hasNonNull("response")) {
                return Optional.empty();
            }
            return Optional.of(new AiRawResponse(name(), cfg.getModel(),
                    response.get("response").asText(), System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.warn("Ollama call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
