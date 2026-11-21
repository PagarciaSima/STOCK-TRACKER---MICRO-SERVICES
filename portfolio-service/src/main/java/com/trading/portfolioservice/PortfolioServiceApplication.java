package com.trading.portfolioservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the portfolio-service: holdings and P&L, driven by Kafka order events.
 */
@SpringBootApplication
public class PortfolioServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioServiceApplication.class, args);
	}

}
