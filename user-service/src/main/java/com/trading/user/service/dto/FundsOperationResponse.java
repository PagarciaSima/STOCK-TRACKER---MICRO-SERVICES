package com.trading.user.service.dto;

import java.math.BigDecimal;

/**
 * Typed response for a funds operation (deduct / credit / add) exposed to
 * Feign clients such as {@code order-service}.
 *
 * @param userId     Identifier of the user. Cannot be null or blank.
 * @param newBalance New wallet balance after the operation. Cannot be null or negative.
 * @param success    Whether the operation succeeded.
 */
public record FundsOperationResponse(String userId, BigDecimal newBalance, boolean success) {
}
