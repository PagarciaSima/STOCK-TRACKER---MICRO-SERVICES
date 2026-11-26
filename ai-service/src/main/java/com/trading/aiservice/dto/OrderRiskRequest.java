package com.trading.aiservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input payload describing an order plus precomputed behavioural features used
 * by the risk engine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRiskRequest {

    /** Identifier of the user placing the order. */
    @NotBlank(message = "userId is required")
    private String userId;

    /** Stock ticker symbol. */
    @NotBlank(message = "symbol is required")
    private String symbol;

    /** Order type (BUY or SELL). */
    @NotBlank(message = "type is required")
    private String type;

    /** Number of shares. */
    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    /** Unit price. */
    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", message = "price must be >= 0")
    private BigDecimal price;

    /** Total order amount (price * quantity). */
    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.0", message = "totalAmount must be >= 0")
    private BigDecimal totalAmount;

    /** Current wallet balance of the user (optional). */
    private BigDecimal walletBalance;

    /** Number of orders placed by the user within the velocity window. */
    private int recentOrderCount;

    /** Whether an identical recent order exists (duplicate signal). */
    private boolean duplicateRecentOrder;

    /** Whether a recent opposite-side order for the same symbol exists (wash-trade signal). */
    private boolean oppositeRecentOrder;

    /** Number of orders placed today. */
    private int dailyOrderCount;

    /** Total turnover (sum of amounts) today. */
    private BigDecimal dailyTurnover;

    /** Whether the symbol is allowed for trading. */
    private boolean symbolAllowed = true;

    /** Whether the user's account is blocked/suspended. */
    private boolean accountBlocked;
}
