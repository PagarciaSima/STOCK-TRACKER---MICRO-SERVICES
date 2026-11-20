package com.trading.notification.service.service;

/**
 * Strategy interface for sending notifications to a user.
 * <p>
 * Implementations can send via log (development), email (JavaMail), SMS (Twilio),
 * push, or any other channel. The active implementation is chosen through the
 * {@code notification.channel} property.
 *
 * @param userId   the identifier of the target user. Cannot be null or blank.
 * @param title    the notification title/subject. Cannot be null or blank.
 * @param message  the notification body message. Cannot be null or blank.
 */
public interface NotificationSender {

    /**
     * Send a notification to the given user.
     *
     * @param userId   the target user. Cannot be null or blank.
     * @param title    the notification title. Cannot be null or blank.
     * @param message  the notification message. Cannot be null or blank.
     */
    void send(String userId, String title, String message);
}
