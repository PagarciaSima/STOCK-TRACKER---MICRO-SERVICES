package com.trading.portfolioservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a user's holding of a single stock (shares owned,
 * average buy price and total invested amount). Unique per user + symbol.
 */
@Entity
@Table(name = "holdings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "symbol"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holding {

    /** Unique identifier (UUID) of the holding. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Identifier of the user who owns the holding. */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Stock ticker symbol. */
    @Column(nullable = false)
    private String symbol;

    /** Display name of the company. */
    @Column(nullable = false)
    private String companyName;

    /** Number of shares currently held. */
    private int quantity;

    /** Average buy price per share. */
    @Column(precision = 15, scale = 2)
    private BigDecimal averageBuyPrice;

    /** Total amount invested in this holding. */
    @Column(precision = 15, scale = 2)
    private BigDecimal totalInvested;

    /** Timestamp of the last update to the holding. */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
