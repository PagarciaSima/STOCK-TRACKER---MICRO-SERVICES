package com.trading.market.dataservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a tradeable stock instrument.
 */
@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /** Ticker symbol (primary key), e.g. "AAPL". */
    @Id
    @Column(nullable = false, unique = true)
    private String symbol;

    /** Display name of the company. */
    @Column(nullable = false)
    private String companyName;

    /** Price at which the instrument starts trading. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal initialPrice;

    /** Exchange where the stock is listed (e.g. NSE, NASDAQ). */
    @Column(nullable = false)
    private String exchange;

    /** Trading currency (e.g. INR, USD, GBP). */
    @Column(nullable = false)
    private String currency;

    /** Whether the stock is available for trading. */
    private boolean active = true;
}
