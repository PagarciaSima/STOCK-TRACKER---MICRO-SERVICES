package com.trading.notification.service.dto;

import java.math.BigDecimal;

/**
 * Typed Kafka event consumed from the {@code user.registered} topic.
 * Replaces the generic {@code Map<String, Object>} used in
 * {@code NotificationService} listeners.
 *
 * @param userId        Identifier of the new user. Cannot be null or blank.
 * @param email         Email address of the new user. Cannot be null or blank.
 * @param firstName     First name of the new user. Cannot be null or blank.
 * @param lastName      Last name of the new user. Cannot be null or blank.
 * @param walletBalance Initial wallet balance of the new user. Cannot be null or negative.
 */
public record UserRegisteredEvent(
        String userId,
        String email,
        String firstName,
        String lastName,
        BigDecimal walletBalance) {
}
