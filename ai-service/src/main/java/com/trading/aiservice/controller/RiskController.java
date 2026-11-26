package com.trading.aiservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.aiservice.dto.OrderRiskRequest;
import com.trading.aiservice.dto.RiskAssessment;
import com.trading.aiservice.service.RiskAssessmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing the AI risk assessment.
 */
@Tag(name = "AI Risk", description = "Rule-based order risk assessment with optional LLM advisory")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class RiskController {

    private final RiskAssessmentService riskAssessmentService;

    /**
     * Assesses the risk of an order.
     *
     * @param request the order and behavioural features. Must be valid.
     * @return {@code 200 OK} with the structured {@link RiskAssessment}.
     */
    @Operation(summary = "Assess an order's risk",
            description = "Evaluates deterministic rules and, in the gray zone, asks an LLM (Ollama, Gemini fallback) for advisory. Returns ALLOW/REVIEW/BLOCK.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/risk/assess")
    public ResponseEntity<RiskAssessment> assess(@Valid @RequestBody OrderRiskRequest request) {
        return ResponseEntity.ok(riskAssessmentService.assess(request));
    }
}
