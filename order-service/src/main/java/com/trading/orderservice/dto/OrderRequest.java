package com.trading.orderservice.dto;

import com.trading.orderservice.entity.OrderType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for placing a buy or sell order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /** Identifier of the user; overridden server-side from the authenticated user id. */
    private String userId;

    /** Stock ticker symbol. Required. */
    @NotBlank(message = "Stock Symbol is required")
    private String symbol;

    /** Order type (BUY or SELL). Required. */
    @NotNull(message = "Order type is required - BUY or SELL")
    private OrderType type;

    /** Number of shares. Must be at least 1. */
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
