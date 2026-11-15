package com.trading.market_dataservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.market_dataservice.dto.StockPrice;
import com.trading.market_dataservice.entity.Stock;
import com.trading.market_dataservice.service.MarketDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/market")
@Slf4j
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    /**
     * Get all stocks with current prices
     */
    @GetMapping("/stocks")
    public ResponseEntity<List<StockPrice>> getAllStocks(){
        return ResponseEntity.ok(
                marketDataService.getAllPrices());
    }
    
    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(
            @PathVariable String symbol) {
        return ResponseEntity.ok(
                marketDataService.getPrice(
                        symbol.toUpperCase()));
    }
    
    /**
     * Get all stocks.
     * Shows which stocks are available for trading.
     * @return
     */
    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getStockList(){

        return ResponseEntity.ok(
                marketDataService.getAllStocks());
    }
}
