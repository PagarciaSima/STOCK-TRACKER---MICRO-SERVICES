package com.trading.user.service.dto;

import java.math.BigDecimal;

/**
 * Typed Kafka event published when a user registers.
 * Replaces the generic {@code Map<String, Object>} used in
 * {@code UserService#register}.
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
