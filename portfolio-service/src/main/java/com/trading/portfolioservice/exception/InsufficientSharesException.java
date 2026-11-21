package com.trading.portfolioservice.exception;

/**
 * Thrown when a SELL operation requests more shares than the user currently holds.
 */
public class InsufficientSharesException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message the detail message. Cannot be null or blank.
     */
    public InsufficientSharesException(String message) {
        super(message);
    }
}
