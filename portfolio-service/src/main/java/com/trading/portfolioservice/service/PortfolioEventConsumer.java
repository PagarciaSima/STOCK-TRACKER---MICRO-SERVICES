package com.trading.portfolioservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.trading.portfolioservice.dto.OrderEvent;
import com.trading.portfolioservice.entity.Holding;
import com.trading.portfolioservice.exception.HoldingNotFoundException;
import com.trading.portfolioservice.exception.InsufficientSharesException;
import com.trading.portfolioservice.repository.HoldingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer that keeps the user portfolio in sync with executed orders.
 * <p>
 * On a {@code BUY} order it adds/updates the holding (recalculating the average
 * buy price); on a {@code SELL} order it reduces or removes the holding.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioEventConsumer {

    private final HoldingRepository holdingRepository;

    /**
     * Consumes an {@code order.executed} event and updates the user's holdings.
     *
     * @param payload the executed order event. Cannot be null.
     */
    @KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(@Payload OrderEvent payload) {
        try {
            String userId = payload.userId();
            String symbol = payload.symbol();
            String type = payload.type();
            int quantity = payload.quantity();
            BigDecimal price = payload.price();

            log.info("Updating portfolio - user: {} {} x {} at {}", userId, symbol, quantity, price);
            if ("BUY".equals(type)) {
                addHolding(userId, symbol, quantity, price);
            } else if ("SELL".equals(type)) {
                removeHolding(userId, symbol, quantity);
            }
        } catch (Exception e) {
            log.error("Error updating portfolio: {}", e.getMessage());
        }
    }

    /**
     * Adds shares to a holding or creates it, recalculating the average buy price.
     *
     * @param userId   the identifier of the user. Cannot be null or blank.
     * @param symbol   the stock ticker symbol. Cannot be null or blank.
     * @param quantity the number of shares bought. Must be greater than or equal to 1.
     * @param price    the unit buy price. Cannot be null or negative.
     */
    private void addHolding(String userId, String symbol, int quantity, BigDecimal price) {

        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol).orElse(null);

        if (holding == null) {
            // First time buying this stock
            holding = new Holding();
            holding.setUserId(userId);
            holding.setSymbol(symbol);
            holding.setCompanyName(symbol);
            holding.setQuantity(quantity);
            holding.setAverageBuyPrice(price);
            holding.setTotalInvested(price.multiply(BigDecimal.valueOf(quantity)));
        } else {
            // Already holding this stock - update average price
            int newQuantity = holding.getQuantity() + quantity;
            BigDecimal newInvested = holding.getTotalInvested()
                    .add(price.multiply(BigDecimal.valueOf(quantity)));

            BigDecimal newAvgPrice = newInvested.divide(
                    BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setAverageBuyPrice(newAvgPrice);
            holding.setTotalInvested(newInvested);
        }
        holdingRepository.save(holding);
        log.info("Holding updated: {} {} shares of {}", userId, holding.getQuantity(), symbol);
    }

    /**
     * Removes shares from a holding, deleting it when the quantity reaches zero.
     *
     * @param userId   the identifier of the user. Cannot be null or blank.
     * @param symbol   the stock ticker symbol. Cannot be null or blank.
     * @param quantity the number of shares sold. Must be greater than or equal to 1.
     * @throws HoldingNotFoundException   if the user does not hold the symbol.
     * @throws InsufficientSharesException if the user holds fewer shares than requested.
     */
    private void removeHolding(String userId, String symbol, int quantity) {

        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new HoldingNotFoundException("Holding not found: " + symbol));
        if (holding.getQuantity() < quantity) {
            throw new InsufficientSharesException(
                    "Insufficient shares to SELL for " + symbol
                            + ": held=" + holding.getQuantity() + ", requested=" + quantity);
        }

        int newQuantity = holding.getQuantity() - quantity;
        if (newQuantity == 0) {
            holdingRepository.delete(holding);
            log.info("Holding removed: {} sold all {} shares", userId, symbol);
        } else {
            BigDecimal newInvested = holding.getAverageBuyPrice()
                    .multiply(BigDecimal.valueOf(newQuantity));

            holding.setQuantity(newQuantity);
            holding.setTotalInvested(newInvested);

            holdingRepository.save(holding);
            log.info("Holding reduced: {} now has {} shares of {}", userId, newQuantity, symbol);
        }
    }
}
