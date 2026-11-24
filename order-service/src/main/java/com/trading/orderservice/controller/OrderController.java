package com.trading.orderservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.entity.Order;
import com.trading.orderservice.security.AuthorizationGuard;
import com.trading.orderservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for placing and querying orders.
 * <p>
 * The authenticated user id and role are received from the API gateway through
 * the {@code X-User-Id} and {@code X-User-Role} headers.
 */
@Tag(name = "Orders", description = "Place and query BUY/SELL stock orders")
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthorizationGuard authorizationGuard;

    /**
     * Places a BUY or SELL order for the authenticated user.
     *
     * @param userId  the authenticated user id (from the gateway). Cannot be null or blank.
     * @param request the order request. Must be valid (symbol not blank, type not null, quantity &gt;= 1).
     * @return {@code 201 Created} with the created order.
     */
    @Operation(summary = "Place a buy or sell order",
            description = "Creates a BUY/SELL order for the authenticated user and publishes a Kafka event.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Invalid order request")
    })
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody OrderRequest request){

        log.info("Received place order request for user: {} symbol: {}", userId, request.getSymbol());
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }

    /**
     * Retrieves a single order by id. Admins can read any order; a standard user
     * can only read their own orders.
     *
     * @param orderId the unique identifier of the order. Cannot be null or blank.
     * @param userId  the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role    the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the order.
     */
    @Operation(summary = "Get an order by id",
            description = "Returns an order. Admins can read any order; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @Parameter(description = "Order identifier", required = true) @PathVariable String orderId,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        log.debug("Fetching order: {} for user: {}", orderId, userId);
        Order order = orderService.getOrder(orderId);
        authorizationGuard.requireOwnerOrAdmin(userId, role, order.getUserId());

        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves all orders of the authenticated user, newest first.
     *
     * @param userId the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the list of orders for the user.
     */
    @Operation(summary = "List my orders",
            description = "Returns all orders of the authenticated user, newest first.")
    @ApiResponse(responseCode = "200", description = "Orders returned")
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String userId){

        log.debug("Fetching orders for user: {}", userId);
        return ResponseEntity.ok(
                orderService.getMyOrders(userId));
    }

    /**
     * Retrieves all orders of an arbitrary user. Admin-only endpoint.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param role   the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the list of orders for the target user.
     */
    @Operation(summary = "List orders of a user (admin)",
            description = "Returns all orders of any user. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned"),
            @ApiResponse(responseCode = "403", description = "Admin privileges required")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireAdmin(role);
        return ResponseEntity.ok(
                orderService.getUserOrders(userId));
    }
}
