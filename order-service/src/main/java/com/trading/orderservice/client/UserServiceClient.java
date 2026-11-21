package com.trading.orderservice.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trading.orderservice.dto.FundsOperationResponse;

/**
 * Feign client for the user-service. Performs wallet fund operations.
 */
@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    /**
     * Deducts funds from a user's wallet (used for BUY orders).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to deduct. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @PostMapping("/api/v1/users/{userId}/funds/deduct")
    FundsOperationResponse deductFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount);

    /**
     * Credits funds to a user's wallet (used for SELL orders).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to credit. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @PostMapping("/api/v1/users/{userId}/funds/credit")
    FundsOperationResponse creditFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount);
}
