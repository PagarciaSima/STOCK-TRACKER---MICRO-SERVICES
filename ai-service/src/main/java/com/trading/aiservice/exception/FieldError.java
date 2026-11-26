package com.trading.aiservice.exception;

/**
 * Detail of a single invalid field in a request.
 *
 * @param field   the field name.
 * @param message the validation message.
 */
public record FieldError(String field, String message) {
}
