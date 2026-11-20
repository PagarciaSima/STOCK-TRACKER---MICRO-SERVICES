package com.trading.user.service.exception;

import java.util.List;

/**
 * Standardized error response body for the user-service REST API.
 *
 * @param status     HTTP status code. Cannot be null.
 * @param error      Short error code (e.g. "Validation Failed"). Cannot be null or blank.
 * @param message    Human-readable description of the error. Cannot be null or blank.
 * @param fieldErrors details of invalid fields, if any (nullable).
 */
public record ApiError(int status, String error, String message, List<FieldError> fieldErrors) {
}
