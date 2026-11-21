package com.trading.portfolioservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.trading.portfolioservice.dto.StockPriceResponse;

/**
 * Feign client for the market-data-service. Retrieves the current price of a stock.
 */
@FeignClient(name = "market-data-service", url = "${market.service.url}")
public interface MarketDataClient {

    /**
     * Retrieves the current price for a given stock symbol.
     *
     * @param symbol the stock ticker symbol. Cannot be null or blank.
     * @return the current {@link StockPriceResponse} for the symbol.
     */
    @GetMapping("/api/v1/market/stocks/{symbol}")
    StockPriceResponse getStockPrice(@PathVariable String symbol);
}
