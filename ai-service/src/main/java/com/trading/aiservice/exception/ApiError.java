package com.trading.aiservice.exception;

import java.util.List;

/**
 * Standardized error response body for the ai-service REST API.
 *
 * @param status      HTTP status code.
 * @param error       short error code.
 * @param message     human-readable description.
 * @param fieldErrors details of invalid fields (nullable).
 */
public record ApiError(int status, String error, String message, List<FieldError> fieldErrors) {
}
