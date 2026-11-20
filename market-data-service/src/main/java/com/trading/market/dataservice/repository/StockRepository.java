package com.trading.market.dataservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.market.dataservice.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, String> {

	List<Stock> findByActiveTrue();
}