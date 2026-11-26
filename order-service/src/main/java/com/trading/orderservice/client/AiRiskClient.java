package com.trading.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.trading.orderservice.dto.OrderRiskRequest;
import com.trading.orderservice.dto.RiskAssessmentResponse;

/**
 * Feign client for the ai-service risk assessment.
 */
@FeignClient(name = "ai-service", url = "${ai.service.url}")
public interface AiRiskClient {

    /**
     * Assesses the risk of an order.
     *
     * @param request the order and behavioural features. Cannot be null.
     * @return the structured risk assessment.
     */
    @PostMapping("/api/v1/ai/risk/assess")
    RiskAssessmentResponse assess(@RequestBody OrderRiskRequest request);
}
