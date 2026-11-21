package com.trading.market.dataservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.market.dataservice.dto.StockPrice;
import com.trading.market.dataservice.entity.Stock;
import com.trading.market.dataservice.service.MarketDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing market-data queries: current stock prices and the
 * list of tradeable stocks.
 */
@RestController
@RequestMapping("/api/v1/market")
@Slf4j
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    /**
     * Returns all stocks with their current prices.
     *
     * @return {@code 200 OK} with the list of current {@link StockPrice} entries.
     */
    @GetMapping("/stocks")
    public ResponseEntity<List<StockPrice>> getAllStocks(){
        log.debug("Fetching all current stock prices");
        return ResponseEntity.ok(
                marketDataService.getAllPrices());
    }

    /**
     * Returns the current price for a given stock symbol.
     *
     * @param symbol the stock ticker symbol. Cannot be null or blank.
     * @return {@code 200 OK} with the current {@link StockPrice} for the symbol.
     */
    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(
            @PathVariable String symbol) {
        log.debug("Fetching price for symbol: {}", symbol);
        return ResponseEntity.ok(
                marketDataService.getPrice(
                        symbol.toUpperCase()));
    }

    /**
     * Returns all stocks available for trading.
     *
     * @return {@code 200 OK} with the list of active {@link Stock} entities.
     */
    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getStockList(){
        log.debug("Fetching list of tradeable stocks");
        return ResponseEntity.ok(
                marketDataService.getAllStocks());
    }
}
