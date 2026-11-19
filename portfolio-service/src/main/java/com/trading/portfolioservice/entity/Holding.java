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
	
	@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    private int quantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal averageBuyPrice;
	
	@Column(precision = 15, scale = 2)
	private BigDecimal totalInvested;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
