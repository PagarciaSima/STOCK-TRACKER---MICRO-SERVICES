package com.trading.notification.service.service;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Log-based implementation of {@link NotificationSender}.
 * Intended for development and testing without external credentials.
 * It only writes the notification to the application logs.
 */
@Component
@Slf4j
public class LogNotificationSender implements NotificationSender {

    @Override
    public void send(String userId, String title, String message) {
        log.info("--------------------------------------------------");
        log.info("NOTIFICATION SENT");
        log.info("TO USER: {}", userId);
        log.info("TITLE: {}", title);
        log.info("MESSAGE: {}", message);
        log.info("--------------------------------------------------");
    }
}
