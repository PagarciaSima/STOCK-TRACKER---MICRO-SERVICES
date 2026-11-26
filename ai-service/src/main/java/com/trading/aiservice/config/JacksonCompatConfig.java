package com.trading.aiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exposes the Jackson 2 {@link ObjectMapper} that {@code AiRiskAdvisor} injects to
 * parse the raw LLM responses.
 *
 * <p>Spring Boot 4 auto-configures Jackson 3 ({@code tools.jackson}) through
 * {@code spring-boot-starter-jackson}, so the {@code com.fasterxml.jackson.databind.ObjectMapper}
 * bean is no longer created by auto-configuration. Jackson 2 is still on the classpath
 * because Kafka's {@code JsonSerializer} and springdoc depend on it, and migrating the
 * service to the new Jackson 3 API is out of scope for this change.</p>
 */
@Configuration
public class JacksonCompatConfig {

    /**
     * Creates the Jackson 2 mapper used for the LLM payload handling.
     *
     * @return a Jackson 2 {@link ObjectMapper} with default configuration.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
