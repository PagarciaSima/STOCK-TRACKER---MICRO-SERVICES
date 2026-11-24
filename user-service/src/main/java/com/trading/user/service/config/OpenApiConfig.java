package com.trading.user.service.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration for the user-service.
 * <p>
 * Declares the JWT bearer security scheme and points requests to the API Gateway.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "v1",
                description = "User registration, authentication, profiles and wallet operations."),
        servers = @Server(url = "http://localhost:8080", description = "API Gateway"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
