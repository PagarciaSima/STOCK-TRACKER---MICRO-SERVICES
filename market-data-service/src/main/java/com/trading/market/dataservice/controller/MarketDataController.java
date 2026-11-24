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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing market-data queries: current stock prices and the
 * list of tradeable stocks.
 */
@Tag(name = "Market Data", description = "Stock prices and tradeable instruments")
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
    @Operation(summary = "Get all current prices", description = "Returns all stocks with their current prices.")
    @ApiResponse(responseCode = "200", description = "Prices returned")
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
    @Operation(summary = "Get price by symbol", description = "Returns the current price for a given stock symbol.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price returned"),
            @ApiResponse(responseCode = "404", description = "Stock not found")
    })
    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(
            @Parameter(description = "Stock ticker symbol", required = true) @PathVariable String symbol) {
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
    @Operation(summary = "List tradeable stocks", description = "Returns all stocks available for trading.")
    @ApiResponse(responseCode = "200", description = "Stocks returned")
    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getStockList(){
        log.debug("Fetching list of tradeable stocks");
        return ResponseEntity.ok(
                marketDataService.getAllStocks());
    }
}
