package com.trading.portfolioservice.exception;

/**
 * Thrown when a holding for a given user and symbol cannot be found.
 */
public class HoldingNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message the detail message. Cannot be null or blank.
     */
    public HoldingNotFoundException(String message) {
        super(message);
    }
}
