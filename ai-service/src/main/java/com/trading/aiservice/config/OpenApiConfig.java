package com.trading.aiservice.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration for the ai-service.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AI Service API",
                version = "v1",
                description = "Rule-based order risk assessment with optional LLM advisory (Ollama, Gemini fallback)."),
        servers = @Server(url = "http://localhost:8080", description = "API Gateway"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
