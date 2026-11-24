package com.trading.notification.service.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Log-based implementation of {@link NotificationSender}.
 * <p>
 * Active when {@code notification.channel=log} or when the property is missing
 * (default). Intended for development and testing without external credentials;
 * it only writes the notification to the application logs.
 */
@Component
@ConditionalOnProperty(name = "notification.channel", havingValue = "log", matchIfMissing = true)
@Slf4j
public class LogNotificationSender implements NotificationSender {

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String userId, String title, String message) {
        log.info("--------------------------------------------------");
        log.info("NOTIFICATION SENT (log channel)");
        log.info("TO USER: {}", userId);
        log.info("TITLE: {}", title);
        log.info("MESSAGE: {}", message);
        log.info("--------------------------------------------------");
    }
}
