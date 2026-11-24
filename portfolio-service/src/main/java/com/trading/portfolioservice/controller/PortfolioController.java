package com.trading.portfolioservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.portfolioservice.dto.PortfolioResponse;
import com.trading.portfolioservice.security.AuthorizationGuard;
import com.trading.portfolioservice.service.PortfolioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing portfolio queries with live P&L.
 * <p>
 * The authenticated user id and role are received from the API gateway through
 * the {@code X-User-Id} and {@code X-User-Role} headers.
 */
@Tag(name = "Portfolio", description = "User holdings and live profit & loss")
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final AuthorizationGuard authorizationGuard;

    /**
     * Returns the portfolio of the authenticated user with live P&L.
     *
     * @param userId the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the user's {@link PortfolioResponse}.
     */
    @Operation(summary = "Get my portfolio", description = "Returns the authenticated user's portfolio with live P&L.")
    @ApiResponse(responseCode = "200", description = "Portfolio returned")
    @GetMapping("/me")
    public ResponseEntity<PortfolioResponse> getMyPortfolio(
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }

    /**
     * Returns the portfolio of an arbitrary user. Admins can access any user; a
     * standard user can only access their own portfolio.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the target user's {@link PortfolioResponse}.
     */
    @Operation(summary = "Get a user's portfolio",
            description = "Returns any user's portfolio. Admins can access any user; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portfolio returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String requesterId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role) {

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                portfolioService.getPortfolio(userId));
    }
}
