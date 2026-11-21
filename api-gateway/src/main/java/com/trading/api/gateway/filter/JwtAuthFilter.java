package com.trading.api.gateway.filter;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that validates JWT tokens and propagates the authenticated
 * user identity to downstream services.
 * <p>
 * For protected endpoints it expects an {@code Authorization: Bearer <token>}
 * header, validates the token signature, and injects the extracted {@code userId}
 * and {@code email} as the {@code X-User-Id} header for internal services.
 * Public endpoints (register, login, health, websocket) are skipped.
 */
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Creates the filter and registers its configuration type.
     */
    public JwtAuthFilter() {
        super(Config.class);
    }

    /**
     * Applies the JWT authentication logic to the incoming request.
     * <p>
     * Skips public endpoints, rejects requests with a missing/invalid bearer
     * token with {@code 401 Unauthorized}, and propagates the authenticated
     * user id to downstream services via the {@code X-User-Id} header.
     *
     * @param config the filter configuration (currently empty). Cannot be null.
     * @return a {@link GatewayFilter} that performs JWT validation and header injection.
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // skip JWT for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint - skipping JWT: {}", path);
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsHeader("Authorization")) {
                log.warn("Missing Authorization header: {}", path);
                return unauthorizedResponse(exchange, "Authorization header missing");
            }

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(exchange, "Invalid Authorization format");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = extractClaims(token);

                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();

                log.debug("Authenticated request - userId: {} email: {}", userId, email);

                ServerHttpRequest modifiedRequest = request
                        .mutate()
                        .header("X-User-Id", userId)
                        .build();
                return chain.filter(
                        exchange.mutate()
                                .request(modifiedRequest)
                                .build());
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Invalid or expired token");
            }
        };
    }

    /**
     * Parses and verifies the JWT, returning its claims.
     *
     * @param token the raw JWT string (without the {@code Bearer } prefix). Cannot be null or blank.
     * @return the verified {@link Claims} payload of the token. Never null.
     * @throws io.jsonwebtoken.JwtException if the token signature is invalid or the token is expired.
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds the HMAC signing key used to verify JWT signatures.
     *
     * @return a {@link SecretKey} derived from the configured JWT secret. Never null.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Writes a {@code 401 Unauthorized} response and completes the exchange.
     *
     * @param exchange the current server web exchange. Cannot be null.
     * @param message  the reason for the rejection (logged, not returned to the client).
     * @return a {@link Mono} that completes when the response is written.
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * Checks whether a request path is public and should bypass JWT validation.
     *
     * @param path the request path. Cannot be null.
     * @return {@code true} if the path is public (register, login, health, websocket),
     *         {@code false} otherwise.
     */
    private boolean isPublicEndpoint(String path) {
        return path.contains("/register")
                || path.contains("/login")
                || path.contains("/health")
                || path.contains("/ws");
    }

    /**
     * Configuration class for the {@link JwtAuthFilter} gateway filter factory.
     * Currently empty; reserved for future per-route options.
     */
    public static class Config {
    }
}
