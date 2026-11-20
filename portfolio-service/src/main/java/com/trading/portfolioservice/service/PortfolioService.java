package com.trading.portfolioservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trading.portfolioservice.client.MarketDataClient;
import com.trading.portfolioservice.dto.HoldingItem;
import com.trading.portfolioservice.dto.PortfolioResponse;
import com.trading.portfolioservice.dto.StockPriceResponse;
import com.trading.portfolioservice.entity.Holding;
import com.trading.portfolioservice.repository.HoldingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final MarketDataClient marketDataClient;

    /**
     * Get Portfolio with current P&L Calculation.
     * <p>
     * For each holding of the user, resolves the current market price, computes
     * the holding value, invested amount, P&L and P&L percentage, then aggregates
     * the totals across all holdings.
     * <p>
     * <b>Performance note (N+1):</b> today we call the market-data service once per
     * holding inside the loop. For portfolios with many holdings this should evolve
     * to a batch price endpoint or parallel calls with {@code CompletableFuture}.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return a {@link PortfolioResponse} with holdings and aggregated P&L metrics.
     *         Never null.
     */
    public PortfolioResponse getPortfolio(String userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        List<HoldingItem> portfolioItems = new ArrayList<>();
        for (Holding holding : holdings) {
            HoldingItem item = calculateHoldingItem(holding);
            if (item != null) {
                portfolioItems.add(item);
            }
        }

        BigDecimal totalInvested = portfolioItems.stream()
                .map(HoldingItem::invested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentValue = portfolioItems.stream()
                .map(HoldingItem::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPnl = currentValue.subtract(totalInvested);
        BigDecimal totalPnLPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new PortfolioResponse(
                userId,
                portfolioItems,
                totalInvested,
                currentValue,
                totalPnl,
                totalPnLPercent,
                totalPnl.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Calculates the {@link HoldingItem} for a single holding.
     * <p>
     * Resolves the current market price from the market-data service and computes
     * the holding's current value, invested amount, P&L and P&L percentage.
     * The division for P&L percentage is protected against a zero invested amount.
     *
     * @param holding the holding entity. Cannot be null.
     * @return a {@link HoldingItem} with computed values, or {@code null} if the
     *         market price could not be resolved (the holding is skipped in the
     *         portfolio aggregation).
     */
    private HoldingItem calculateHoldingItem(Holding holding) {
        try {
            // Get current market price (one Feign call per holding -> see N+1 note in getPortfolio)
            StockPriceResponse priceData = marketDataClient.getStockPrice(holding.getSymbol());
            BigDecimal currentPrice = priceData.price();

            BigDecimal holdingValue = currentPrice.multiply(
                    BigDecimal.valueOf(holding.getQuantity()));
            BigDecimal invested = holding.getTotalInvested();
            BigDecimal pnl = holdingValue.subtract(invested);

            BigDecimal pnlPercent = invested.compareTo(BigDecimal.ZERO) > 0
                    ? pnl.divide(invested, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return setItem(holding, currentPrice, holdingValue, invested, pnl, pnlPercent);
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", holding.getSymbol(), e.getMessage());
            return null;
        }
    }

    /**
     * Build a {@link HoldingItem} from a holding and its current market data.
     *
     * @param holding      the holding entity. Cannot be null.
     * @param currentPrice the current market price per share. Cannot be null or negative.
     * @param holdingValue the current market value of the holding. Cannot be null or negative.
     * @param invested     the total amount invested in this holding. Cannot be null or negative.
     * @param pnl          the profit and loss for this holding. Cannot be null.
     * @param pnlPercent   the P&L percentage. Cannot be null.
     * @return a {@link HoldingItem} with all computed values. Never null.
     */
    private HoldingItem setItem(Holding holding, BigDecimal currentPrice,
                                BigDecimal holdingValue, BigDecimal invested,
                                BigDecimal pnl, BigDecimal pnlPercent) {
        return new HoldingItem(
                holding.getSymbol(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                currentPrice,
                holdingValue,
                invested,
                pnl,
                pnlPercent,
                pnl.compareTo(BigDecimal.ZERO) > 0);
    }
}
