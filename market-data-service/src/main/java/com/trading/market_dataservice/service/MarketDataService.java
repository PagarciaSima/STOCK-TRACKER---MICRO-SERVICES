package com.trading.market_dataservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.trading.market_dataservice.dto.StockPrice;
import com.trading.market_dataservice.entity.Stock;
import com.trading.market_dataservice.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MarketDataService {

	private final StockRepository stockRepository;
	private final Random random = new Random();
	private final RedisTemplate<String, Object> redisTemplate;
	private final SimpMessagingTemplate messagingTemplate;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String PRICE_KEY_PREFIX = "stock:price:";
	private static final String PRICE_UPDATED_TOPIC = "stock.price.updated";
    
    /**
     * ConcurrentHashMap - for thread Safety
     */
    private final Map<String, StockPrice> stockPrices = 
            new ConcurrentHashMap<>();


    /**
     * Initialize Stock prices
     * 
     * @PostConstruct -> runs when context is ready
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
            
            // 2. Broadcast via WebSocket -> all clients
            messagingTemplate.convertAndSend(
                "/topic/prices/" + symbol, currentPrice);

            // Publish to Kafka
            Map<String, Object> priceUpdatedEvent = new HashMap<>();
            priceUpdatedEvent.put("symbol", symbol);
            priceUpdatedEvent.put("price", newPrice);
            priceUpdatedEvent.put("timestamp",
                    LocalDateTime.now().toString());

            kafkaTemplate.send(PRICE_UPDATED_TOPIC, symbol, priceUpdatedEvent);
        });
        
        log.debug("Prices updated for {} stocks", 
                stockPrices.size());
    }
    
    /**
     * Get current price from Redis
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
     * Get all active stock prices
     * @return
     */
    public List<StockPrice> getAllPrices(){
        return stockPrices.values().stream().toList();
    }
    
    /**
     * Get all stocks
     */
    public List<Stock> getAllStocks(){
        return stockRepository.findByActiveTrue();
    }
}