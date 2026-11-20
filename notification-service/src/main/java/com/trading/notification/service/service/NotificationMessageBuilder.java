package com.trading.notification.service.service;

import com.trading.notification.service.dto.OrderEvent;
import com.trading.notification.service.dto.UserRegisteredEvent;
import java.math.BigDecimal;

/**
 * Builds notification titles and messages from typed Kafka events.
 * <p>
 * Centralizes message construction so the {@link NotificationService} listeners
 * remain thin and focused on orchestration, not string formatting.
 */
public class NotificationMessageBuilder {

    /**
     * Builds the title for an executed order notification.
     *
     * @param event the executed order event. Cannot be null.
     * @return the notification title. Never null.
     */
    public String buildOrderExecutedTitle(OrderEvent event) {
        return "ORDER EXECUTED - " + event.type();
    }

    /**
     * Builds the message for an executed order notification.
     *
     * @param event the executed order event. Cannot be null.
     * @return the notification message. Never null.
     */
    public String buildOrderExecutedMessage(OrderEvent event) {
        return String.format(
                "%s %d shares of %s at %s. Total: %s",
                event.type(), event.quantity(), event.symbol(),
                event.price(), event.totalAmount());
    }

    /**
     * Builds the title for a failed order notification.
     *
     * @return the notification title. Never null.
     */
    public String buildOrderFailedTitle() {
        return "Order Failed";
    }

    /**
     * Builds the message for a failed order notification.
     *
     * @param symbol the stock symbol. Cannot be null or blank.
     * @param reason the failure reason. Cannot be null or blank.
     * @return the notification message. Never null.
     */
    public String buildOrderFailedMessage(String symbol, String reason) {
        return String.format("Your order for %s failed. Reason: %s", symbol, reason);
    }

    /**
     * Builds the title for a user-registered notification.
     *
     * @return the notification title. Never null.
     */
    public String buildUserRegisteredTitle() {
        return "Welcome to Stock Trading Platform!";
    }

    /**
     * Builds the message for a user-registered notification.
     *
     * @param event the user-registered event. Cannot be null.
     * @return the notification message. Never null.
     */
    public String buildUserRegisteredMessage(UserRegisteredEvent event) {
        return String.format(
                "Welcome %s!  %s added to your wallet.",
                event.firstName(), event.walletBalance());
    }
}
