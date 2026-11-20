package com.trading.orderservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trading.orderservice.client.MarketDataClient;
import com.trading.orderservice.client.UserServiceClient;
import com.trading.orderservice.dto.FundsOperationResponse;
import com.trading.orderservice.dto.OrderEvent;
import com.trading.orderservice.dto.OrderRequest;
import com.trading.orderservice.dto.StockPriceResponse;
import com.trading.orderservice.entity.Order;
import com.trading.orderservice.entity.OrderStatus;
import com.trading.orderservice.entity.OrderType;
import com.trading.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final MarketDataClient marketDataClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final OrderRepository orderRepository;
    private static final String ORDER_EXECUTED_TOPIC = "order.executed";
    private static final String ORDER_FAILED_TOPIC = "order.failed";

    /**
     * Place a BUY or SELL order.
     * <p>
     * Flow:
     * <ol>
     *     <li>Resolve the current market price for the symbol.</li>
     *     <li>Calculate the total amount (price * quantity).</li>
     *     <li>Create and persist the order with {@code PENDING} status.</li>
     *     <li>Execute the payment (deduct for BUY, credit for SELL).</li>
     *     <li>Mark the order as {@code EXECUTED} and publish a Kafka event.</li>
     * </ol>
     * If any step fails, the order is marked as {@code FAILED} and a failure
     * event is published to Kafka.
     *
     * @param request the order request containing userId, symbol, type and quantity.
     *                {@code symbol} and {@code type} must not be blank; {@code quantity} must be &gt;= 1.
     * @return the saved order, with status {@code EXECUTED} on success or {@code FAILED} on error.
     * @throws RuntimeException if the market price cannot be resolved or the payment execution fails.
     */
    public Order placeOrder(OrderRequest request) {
        log.info("Placing {} order: {} x {} for user: {}",
                request.getType(), request.getQuantity(),
                request.getSymbol(), request.getUserId());

        BigDecimal currentPrice = resolveCurrentPrice(request.getSymbol());
        BigDecimal totalAmount = calculateTotalAmount(currentPrice, request.getQuantity());

        Order order = createOrder(request, currentPrice, totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order Created: {}", savedOrder.getId());

        try {
            executePayment(request, totalAmount);

            savedOrder.setStatus(OrderStatus.EXECUTED);
            savedOrder.setExecutedAt(LocalDateTime.now());
            Order executedOrder = orderRepository.save(savedOrder);

            log.info("Order EXECUTED: {} {} x {} at {}",
                    request.getType(), request.getQuantity(),
                    request.getSymbol(), currentPrice);
            publishOrderEvent(ORDER_EXECUTED_TOPIC, executedOrder, null);

            return executedOrder;
        } catch (Exception e) {
            savedOrder.setStatus(OrderStatus.FAILED);
            savedOrder.setFailureReason(e.getMessage());
            Order failedOrder = orderRepository.save(savedOrder);

            log.error("Order FAILED: {} reason: {}",
                    savedOrder.getId(), e.getMessage());
            publishOrderEvent(ORDER_FAILED_TOPIC, failedOrder, e.getMessage());

            return failedOrder;
        }
    }

    /**
     * Resolve the current market price for the given symbol via the market-data client.
     *
     * @param symbol the stock ticker symbol. Cannot be null or blank.
     * @return the current unit price. Never null.
     */
    private BigDecimal resolveCurrentPrice(String symbol) {
        StockPriceResponse priceData = marketDataClient.getStockPrice(symbol);
        BigDecimal currentPrice = priceData.price();
        log.debug("Resolved price for {}: {}", symbol, currentPrice);
        return currentPrice;
    }

    /**
     * Calculate the total amount multiplying unit price by quantity.
     *
     * @param price    the unit price of the stock. Cannot be null or negative.
     * @param quantity the number of shares. Must be greater than or equal to 1.
     * @return the total amount rounded to 2 decimal places.
     */
    private BigDecimal calculateTotalAmount(BigDecimal price, int quantity) {
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        log.debug("Calculated total amount: {} (price={} quantity={})", total, price, quantity);
        return total;
    }

    /**
     * Build an {@code Order} entity from the request, price and total amount.
     *
     * @param request     the order request. Cannot be null.
     * @param currentPrice the resolved unit price. Cannot be null.
     * @param totalAmount the calculated total amount. Cannot be null.
     * @return a new {@code Order} with {@code PENDING} status.
     */
    private Order createOrder(OrderRequest request, BigDecimal currentPrice, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setSymbol(request.getSymbol());
        order.setType(request.getType());
        order.setQuantity(request.getQuantity());
        order.setPrice(currentPrice);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    /**
     * Execute the payment against the user-service: deduct funds for BUY orders
     * or credit funds for SELL orders.
     *
     * @param request     the order request. Cannot be null.
     * @param totalAmount the total amount to deduct or credit. Cannot be null or negative.
     */
    private void executePayment(OrderRequest request, BigDecimal totalAmount) {
        if (request.getType() == OrderType.BUY) {
            FundsOperationResponse response = userServiceClient.deductFunds(request.getUserId(), totalAmount);
            log.debug("Funds deducted for user {}: newBalance={} success={}",
                    request.getUserId(), response.newBalance(), response.success());
        } else {
            FundsOperationResponse response = userServiceClient.creditFunds(request.getUserId(), totalAmount);
            log.debug("Funds credited for user {}: newBalance={} success={}",
                    request.getUserId(), response.newBalance(), response.success());
        }
    }

    /**
     * Publish a typed {@link OrderEvent} to the given Kafka topic.
     *
     * @param topic  the Kafka topic name (e.g. {@code order.executed} or {@code order.failed}).
     *               Cannot be null or blank.
     * @param order  the order to publish. Cannot be null.
     * @param reason the failure reason, if any (nullable).
     */
    private void publishOrderEvent(String topic, Order order, String reason) {
        OrderEvent event = new OrderEvent(
                order.getId(),
                order.getUserId(),
                order.getSymbol(),
                order.getType().name(),
                order.getQuantity(),
                order.getPrice(),
                order.getTotalAmount(),
                order.getStatus().name(),
                reason);
        kafkaTemplate.send(topic, order.getId(), event);
        log.info("Event published: {} for order: {}", topic, order.getId());
    }

    /**
     * Retrieve an order by its identifier.
     *
     * @param orderId the unique identifier of the order. Cannot be null or blank.
     * @return the found order.
     * @throws RuntimeException if no order exists with the given identifier.
     */
    /**
     * Retrieve an order by its identifier.
     *
     * @param orderId the unique identifier of the order. Cannot be null or blank.
     * @return the found order.
     * @throws RuntimeException if no order exists with the given identifier.
     */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * Retrieve all orders for a user, sorted by creation date descending.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return the list of orders for the user (possibly empty). Never null.
     */
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Retrieve all orders for the authenticated user, sorted by creation date descending.
     *
     * @param userId the identifier of the authenticated user. Cannot be null or blank.
     * @return the list of orders for the user (possibly empty). Never null.
     */
    public List<Order> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
