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
import com.trading.orderservice.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for placing and querying orders.
 * <p>
 * The authenticated user id is received from the API gateway through the
 * {@code X-User-Id} header.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Places a BUY or SELL order for the authenticated user.
     * <p>
     * The {@code userId} from the request body is overridden with the
     * authenticated user id from the {@code X-User-Id} header to prevent
     * clients from impersonating other users.
     *
     * @param userId  the authenticated user id (from the gateway). Cannot be null or blank.
     * @param request the order request. Must be valid (symbol not blank, type not null, quantity &gt;= 1).
     * @return {@code 201 Created} with the created order.
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody OrderRequest request){

        log.info("Received place order request for user: {} symbol: {}", userId, request.getSymbol());
        request.setUserId(userId); // override with authenticated user id
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }

    /**
     * Retrieves a single order by id, enforcing ownership.
     *
     * @param orderId the unique identifier of the order. Cannot be null or blank.
     * @param userId  the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the order if it belongs to the user;
     *         {@code 403 Forbidden} if the order belongs to another user.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String userId){

        log.debug("Fetching order: {} for user: {}", orderId, userId);
        Order order = orderService.getOrder(orderId);

        // Security check: only the owner can read the order
        if (!order.getUserId().equals(userId)) {
            log.warn("User {} attempted to access order {} owned by {}", userId, orderId, order.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves all orders of the authenticated user, newest first.
     *
     * @param userId the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the list of orders for the user.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(
            @RequestHeader("X-User-Id") String userId){

        log.debug("Fetching orders for user: {}", userId);
        return ResponseEntity.ok(
                orderService.getMyOrders(userId));
    }
}
