package com.trading.orderservice.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trading.orderservice.dto.FundsOperationResponse;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @PostMapping("/api/v1/users/{userId}/funds/deduct")
    FundsOperationResponse deductFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount);

    @PostMapping("/api/v1/users/{userId}/funds/credit")
    FundsOperationResponse creditFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount);
}
