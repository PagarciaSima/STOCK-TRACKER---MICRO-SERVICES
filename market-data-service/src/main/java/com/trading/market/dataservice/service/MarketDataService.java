package com.trading.market.dataservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trading.market.dataservice.dto.PriceUpdatedEvent;
import com.trading.market.dataservice.dto.StockPrice;
import com.trading.market.dataservice.entity.Stock;
import com.trading.market.dataservice.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core service for market data: loads stocks, simulates live price movements,
 * caches the latest price in Redis, broadcasts updates over WebSocket and
 * publishes price-update events to Kafka.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDataService {

	private final StockRepository stockRepository;
	private final Random random = new Random();
	private final RedisTemplate<String, Object> redisTemplate;
	private final SimpMessagingTemplate messagingTemplate;
	private final KafkaTemplate<String, PriceUpdatedEvent> kafkaTemplate;

	private static final String PRICE_KEY_PREFIX = "stock:price:";
	private static final String PRICE_UPDATED_TOPIC = "stock.price.updated";
    
    /**
     * ConcurrentHashMap - for thread Safety
     */
    private final Map<String, StockPrice> stockPrices = 
            new ConcurrentHashMap<>();


    /**
     * Loads all active stocks from the database into the in-memory price map.
     * <p>
     * Runs once after the application context is ready ({@code @PostConstruct}).
     * If no active stocks exist, logs a warning and returns without initializing.
     */
    @PostConstruct
    public void initializePrices(){
        log.info("Loading stock data....");

        List<Stock> stocks = stockRepository.findByActiveTrue();

        if(stocks.isEmpty()){
            log.warn("No active stocks found!");
            return;
        }

        stocks.forEach( stock -> {
            StockPrice stockPrice = new StockPrice();
            stockPrice.setSymbol(stock.getSymbol());
            stockPrice.setCompanyName(stock.getCompanyName());
            stockPrice.setPrice(stock.getInitialPrice());
            stockPrice.setOpen(stock.getInitialPrice());
            stockPrice.setHigh(stock.getInitialPrice());
            stockPrice.setLow(stock.getInitialPrice());
            stockPrice.setChange(BigDecimal.ZERO);
            stockPrice.setChangePercent(BigDecimal.ZERO);
            stockPrice.setVolume(0L);
            stockPrice.setTimestamp(LocalDateTime.now());

            stockPrices.put(stock.getSymbol(), stockPrice);
            
            // Log por cada acción cargada
            log.info("Loaded Stock: {} at {}", 
                    stock.getSymbol(), stock.getInitialPrice());
        });

        // Log final con el total de acciones cargadas
        log.info("Initialized {} stocks", 
                stockPrices.size());
    }
    
    /**
     * Simulates a price movement for every loaded stock on a fixed schedule.
     * <p>
     * For each stock, computes a random price delta, updates high/low/change
     * metrics, caches the new price in Redis, broadcasts it over the
     * {@code /topic/prices/{symbol}} WebSocket topic and publishes a
     * {@link PriceUpdatedEvent} to Kafka.
     */
    @Scheduled(fixedRateString = "${market.price-update-interval}")
    public void updatePrices(){

        if(stockPrices.isEmpty()){
            log.warn("No stocks loaded");
            return;
        }

        stockPrices.forEach((String symbol, StockPrice currentPrice) -> {
            BigDecimal oldPrice = currentPrice.getPrice();

            // +- 0.25 price movement
            double priceMovement = 
                    (random.nextDouble() - 0.5) * 0.5;
            
            BigDecimal priceChange = oldPrice
                    .multiply(BigDecimal.valueOf(
                            priceMovement / 100))
                    .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal newPrice = oldPrice
                    .add(priceChange)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // Prevent negative price
            if(newPrice.compareTo(BigDecimal.valueOf(1)) < 0){
                newPrice = BigDecimal.valueOf(1);
            }

            BigDecimal changeAmount = newPrice
                    .subtract(currentPrice.getOpen())
                    .setScale(1, RoundingMode.HALF_UP);
            
            BigDecimal changePercent = changeAmount
                    .divide(currentPrice.getOpen(), 4,
                            RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            
            // Update high/low
            BigDecimal newHigh = newPrice
                    .compareTo(currentPrice.getHigh()) > 0
                    ? newPrice : currentPrice.getHigh();

            BigDecimal newLow = newPrice
                    .compareTo(currentPrice.getLow()) < 0
                    ? newPrice : currentPrice.getLow();
            
            currentPrice.setPrice(newPrice);
            currentPrice.setChange(changeAmount);
            currentPrice.setChangePercent(changePercent);
            currentPrice.setHigh(newHigh);
            currentPrice.setLow(newLow);
            currentPrice.setTimestamp(LocalDateTime.now());
            
            // 1. Cache in Redis
            redisTemplate.opsForValue().set(
                PRICE_KEY_PREFIX + symbol, currentPrice);
            
            // 2. Broadcast via WebSocket -> all clients (frontend) subscribed to this topic will receive the update
            messagingTemplate.convertAndSend(
                "/topic/prices/" + symbol, currentPrice);

            // Publish to Kafka -> other microservices can consume this event
            PriceUpdatedEvent priceUpdatedEvent = new PriceUpdatedEvent(
                    symbol,
                    newPrice,
                    LocalDateTime.now());

            kafkaTemplate.send(PRICE_UPDATED_TOPIC, symbol, priceUpdatedEvent);
        });
        
        log.debug("Prices updated for {} stocks", 
                stockPrices.size());
    }
    
    /**
     * Returns the latest price for a symbol, reading from Redis first and
     * falling back to the in-memory map.
     *
     * @param symbol the stock ticker symbol. Cannot be null or blank.
     * @return the current {@link StockPrice}. Never null.
     * @throws RuntimeException if the symbol is not cached nor loaded in memory.
     */
    public StockPrice getPrice(String symbol){
        Object cached = redisTemplate.opsForValue()
                .get(PRICE_KEY_PREFIX + symbol);

        if(cached != null){
            return (StockPrice) cached;
        }

        // Fallback
        StockPrice price = stockPrices.get(
                symbol.toUpperCase());
        
        if(price == null){
            throw new RuntimeException(
                "Stock not found: " + symbol);
        }
        return price;
    }
    
    /**
     * Returns the current prices of all loaded stocks.
     *
     * @return an immutable list of {@link StockPrice} (possibly empty). Never null.
     */
    public List<StockPrice> getAllPrices(){
        return stockPrices.values().stream().toList();
    }
    
    /**
     * Returns all active stocks available for trading.
     *
     * @return a list of active {@link Stock} entities (possibly empty). Never null.
     */
    public List<Stock> getAllStocks(){
        return stockRepository.findByActiveTrue();
    }
}