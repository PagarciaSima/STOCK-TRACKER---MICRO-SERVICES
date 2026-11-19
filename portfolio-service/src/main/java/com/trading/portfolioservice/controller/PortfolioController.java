package com.trading.portfolioservice.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.portfolioservice.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Get my portfolio with live P & L
     * @param userId
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyPortfolio(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }
    
    /**
     * Get portfolio by userId - For internal calls
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPortfolio(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }
}
