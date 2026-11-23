package com.trading.portfolioservice.exception;

/**
 * Thrown when an authenticated user attempts an action they are not allowed to
 * perform (e.g. accessing another user's portfolio or an admin-only endpoint).
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message the detail message. Cannot be null or blank.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
