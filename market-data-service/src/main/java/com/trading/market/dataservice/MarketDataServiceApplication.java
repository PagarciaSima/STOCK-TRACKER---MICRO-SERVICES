package com.trading.market.dataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the market-data-service: stock prices, Redis cache and WebSocket streaming.
 */
@SpringBootApplication
@EnableScheduling
public class MarketDataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketDataServiceApplication.class, args);
	}

}
