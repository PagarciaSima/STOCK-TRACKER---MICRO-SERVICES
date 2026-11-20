package com.trading.notification.service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.trading.notification.service.dto.OrderEvent;
import com.trading.notification.service.dto.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSender notificationSender;
    private final NotificationMessageBuilder messageBuilder;

    /**
     * Consumes an {@code order.executed} Kafka event and sends a notification.
     *
     * @param event the executed order event. Cannot be null.
     */
    @KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(@Payload OrderEvent event) {
        try {
            String title = messageBuilder.buildOrderExecutedTitle(event);
            String message = messageBuilder.buildOrderExecutedMessage(event);
            notificationSender.send(event.userId(), title, message);
            log.info("Notification sent for executed order: {} user: {}",
                    event.orderId(), event.userId());
        } catch (Exception e) {
            log.error("Error sending order executed notification: {}", e.getMessage());
        }
    }

    /**
     * Consumes an {@code order.failed} Kafka event and sends a notification.
     *
     * @param event the failed order event. Cannot be null.
     */
    @KafkaListener(topics = "order.failed")
    public void consumeOrderFailed(@Payload OrderEvent event) {
        try {
            String title = messageBuilder.buildOrderFailedTitle();
            String message = messageBuilder.buildOrderFailedMessage(
                    event.symbol(), event.reason() != null ? event.reason() : "unknown");
            notificationSender.send(event.userId(), title, message);
            log.info("Notification sent for failed order: {} user: {}",
                    event.orderId(), event.userId());
        } catch (Exception e) {
            log.error("Error sending order failed notification: {}", e.getMessage());
        }
    }

    /**
     * Consumes a {@code user.registered} Kafka event and sends a welcome notification.
     *
     * @param event the user-registered event. Cannot be null.
     */
    @KafkaListener(topics = "user.registered")
    public void consumeUserRegistered(@Payload UserRegisteredEvent event) {
        try {
            String title = messageBuilder.buildUserRegisteredTitle();
            String message = messageBuilder.buildUserRegisteredMessage(event);
            notificationSender.send(event.userId(), title, message);
            log.info("Welcome notification sent for user: {}", event.userId());
        } catch (Exception e) {
            log.error("Error sending user registered notification: {}", e.getMessage());
        }
    }
}
