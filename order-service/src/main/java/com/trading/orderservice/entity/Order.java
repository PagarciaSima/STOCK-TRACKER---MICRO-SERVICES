package com.trading.orderservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a buy or sell order placed by a user.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /** Unique identifier (UUID) of the order. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Identifier of the user who placed the order. */
    @Column(nullable = false)
    private String userId;

    /** Stock ticker symbol. */
    @Column(nullable = false)
    private String symbol;

    /** Order type: BUY or SELL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    /** Current status of the order: PENDING, EXECUTED or FAILED. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** Number of shares. */
    @Column(nullable = false)
    private int quantity;

    /** Unit price at execution. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** Total amount (price * quantity). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /** Reason of the failure, if the order failed. */
    private String failureReason;

    /** Timestamp when the order was created. */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** Timestamp when the order was executed, if applicable. */
    private LocalDateTime executedAt;

}
