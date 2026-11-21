package com.trading.market.dataservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.market.dataservice.entity.Stock;

/**
 * Spring Data JPA repository for {@link Stock} entities.
 */
public interface StockRepository extends JpaRepository<Stock, String> {

    /**
     * Finds all active stocks available for trading.
     *
     * @return the list of active stocks (possibly empty). Never null.
     */
    List<Stock> findByActiveTrue();
}
