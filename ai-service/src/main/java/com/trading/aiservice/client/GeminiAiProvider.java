package com.trading.aiservice.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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
 * LLM provider backed by the Google Gemini API ({@code generateContent}).
 * Used as a cloud fallback when Ollama is unavailable.
 */
@Component
@Slf4j
public class GeminiAiProvider implements AiProvider {

    private final AiProperties.Gemini cfg;
    private final RestClient restClient;

    /**
     * Creates the provider with connect/read timeouts from configuration.
     *
     * @param properties the ai properties. Cannot be null.
     */
    public GeminiAiProvider(AiProperties properties) {
        this.cfg = properties.getGemini();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(cfg.getTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(cfg.getTimeoutMs()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return cfg.isEnabled() && cfg.getApiKey() != null && !cfg.getApiKey().isBlank();
    }

    @Override
    public Optional<AiRawResponse> generate(String prompt) {
        if (!isAvailable()) {
            return Optional.empty();
        }
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            body.put("generationConfig", Map.of("temperature", 0, "responseMimeType", "application/json"));

            String url = cfg.getBaseUrl() + "/v1beta/models/" + cfg.getModel()
                    + ":generateContent?key=" + cfg.getApiKey();

            JsonNode response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return Optional.empty();
            }
            JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text.isMissingNode() || text.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new AiRawResponse(name(), cfg.getModel(),
                    text.asText(), System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.warn("Gemini call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
