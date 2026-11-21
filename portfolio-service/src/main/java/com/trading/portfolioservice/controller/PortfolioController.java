package com.trading.portfolioservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.portfolioservice.dto.PortfolioResponse;
import com.trading.portfolioservice.service.PortfolioService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing portfolio queries with live P&L.
 * <p>
 * The authenticated user id is received from the API gateway through the
 * {@code X-User-Id} header.
 */
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Returns the portfolio of the authenticated user with live P&L.
     *
     * @param userId the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the user's {@link PortfolioResponse}.
     */
    @GetMapping("/me")
    public ResponseEntity<PortfolioResponse> getMyPortfolio(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }

    /**
     * Returns the portfolio of an arbitrary user. Intended for internal or
     * administrative calls.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @return {@code 200 OK} with the target user's {@link PortfolioResponse}.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }
}
