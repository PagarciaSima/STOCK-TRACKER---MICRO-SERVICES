package com.trading.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Global gateway filter that blocks external access to service-to-service
 * internal endpoints (paths containing {@code /internal/}).
 * <p>
 * Internal endpoints are called directly by other microservices (e.g. Feign)
 * and must never be reachable through the public API gateway.
 */
@Component
@Slf4j
public class InternalEndpointBlockFilter implements GlobalFilter, Ordered {

    private static final String INTERNAL_SEGMENT = "/internal/";

    /**
     * Rejects requests whose path targets an internal endpoint with {@code 403 Forbidden}.
     *
     * @param exchange the current server web exchange. Cannot be null.
     * @param chain    the gateway filter chain. Cannot be null.
     * @return a {@link Mono} that completes after either blocking or forwarding the request.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (path.contains(INTERNAL_SEGMENT)) {
            log.warn("Blocked external access to internal endpoint: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    /**
     * Runs before other filters so internal paths are rejected as early as possible.
     *
     * @return the filter order (highest precedence).
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
