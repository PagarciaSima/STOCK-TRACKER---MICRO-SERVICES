package com.trading.aiservice.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for the ai-service ({@code ai.*}).
 */
@Component
@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {

    /** Provider strategy: ollama | gemini | auto. */
    private String provider = "auto";

    /** Consult the LLM only when the rule engine returns REVIEW. */
    private boolean consultOnReview = true;

    private Ollama ollama = new Ollama();
    private Gemini gemini = new Gemini();
    private Risk risk = new Risk();

    /** Ollama (local) settings. */
    @Data
    public static class Ollama {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.2:3b";
        private int timeoutMs = 8000;
    }

    /** Gemini (cloud fallback) settings. */
    @Data
    public static class Gemini {
        private boolean enabled = false;
        private String baseUrl = "https://generativelanguage.googleapis.com";
        private String model = "gemini-1.5-flash";
        private String apiKey = "";
        private int timeoutMs = 8000;
    }

    /** Risk thresholds and limits. */
    @Data
    public static class Risk {
        private BigDecimal maxOrderAmount = BigDecimal.valueOf(50000);
        private BigDecimal balanceRatioThreshold = BigDecimal.valueOf(0.5);
        private int velocityWindowMinutes = 5;
        private int velocityMaxOrders = 20;
        private int mediumScoreThreshold = 40;
        private int highScoreThreshold = 70;
    }
}
