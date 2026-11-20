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
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return a {@link PortfolioResponse} with holdings and aggregated P&L metrics.
     *         Never null.
     */
    public PortfolioResponse getPortfolio(String userId) {
        List<Holding> holdings = holdingRepository
                .findByUserId(userId);

        List<HoldingItem> portfolioItems = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            try {
                // Get current market price
                StockPriceResponse priceData = marketDataClient
                        .getStockPrice(holding.getSymbol());

                BigDecimal currentPrice = priceData.price();

                BigDecimal holdingValue = currentPrice.multiply(
                        BigDecimal.valueOf(holding.getQuantity()));
                BigDecimal invested = holding.getTotalInvested();
                // Profit & loss
                BigDecimal pnl = holdingValue.subtract(invested);

                BigDecimal pnlPercent = pnl.divide(invested, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                HoldingItem item = setItem(holding, currentPrice, holdingValue,
                        invested, pnl, pnlPercent);
                portfolioItems.add(item);
                totalInvested = totalInvested.add(invested);
                currentValue = currentValue.add(holdingValue);
            } catch (Exception e) {
                log.error("Error fetching price for {}: {}",
                        holding.getSymbol(), e.getMessage());
            }
        }

        BigDecimal totalPnl = currentValue.subtract(totalInvested);
        BigDecimal totalPnLPercent = totalInvested
                .compareTo(BigDecimal.ZERO) > 0
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
