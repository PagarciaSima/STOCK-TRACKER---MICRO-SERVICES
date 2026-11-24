package com.trading.notification.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Email implementation of {@link NotificationSender} using Spring Mail.
 * <p>
 * Active only when {@code notification.channel=email}. The recipient is taken
 * from {@code notification.email.to} (a demo recipient; a production
 * implementation would resolve the user's email per user).
 */
@Component
@ConditionalOnProperty(name = "notification.channel", havingValue = "email")
@Slf4j
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${notification.email.to:}")
    private String to;

    /**
     * Creates the sender.
     *
     * @param mailSender the configured {@link JavaMailSender}. Cannot be null.
     */
    public EmailNotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String userId, String title, String message) {
        if (to == null || to.isBlank()) {
            log.warn("Email channel selected but notification.email.to is not configured; "
                    + "skipping notification (user={})", userId);
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                mail.setFrom(from);
            }
            mail.setTo(to);
            mail.setSubject(title);
            mail.setText(message);
            mailSender.send(mail);
            log.info("Email notification sent to {} (user={})", to, userId);
        } catch (Exception e) {
            log.error("Error sending email notification: {}", e.getMessage());
        }
    }
}
