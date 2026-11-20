package com.trading.portfolioservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.trading.portfolioservice.dto.StockPriceResponse;

@FeignClient(name = "market-data-service", url = "${market.service.url}")
public interface MarketDataClient {

    @GetMapping("/api/v1/market/stocks/{symbol}")
    StockPriceResponse getStockPrice(@PathVariable String symbol);
}