package com.trading.market.dataservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket (STOMP) configuration for streaming live stock prices.
 * <p>
 * How it works:
 * <ol>
 *   <li>Client connects to the {@code /ws} endpoint.</li>
 *   <li>Client subscribes to {@code /topic/prices/{symbol}}.</li>
 *   <li>Server publishes price updates to that topic.</li>
 *   <li>Client receives updates in real time — no polling needed.</li>
 * </ol>
 * This is how trading platforms stream real-time prices.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker and the application destination prefix.
     * <p>
     * Enables a simple in-memory broker on the {@code /topic} prefix (broadcast
     * to all subscribers) and routes client messages sent to {@code /app/**}
     * to {@code @MessageMapping} methods.
     *
     * @param registry the message broker registry. Cannot be null.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        // Topics prefixed with /topic -> broadcast to all subscribers
        registry.enableSimpleBroker("/topic");

        // Application destination prefix:
        // client messages to /app/** are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the STOMP endpoint clients connect to.
     * <p>
     * Exposes the {@code /ws} endpoint with SockJS fallback for browsers that
     * do not support native WebSocket.
     *
     * @param registry the STOMP endpoint registry. Cannot be null.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // WebSocket endpoint - clients connect here
        // SockJS fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
