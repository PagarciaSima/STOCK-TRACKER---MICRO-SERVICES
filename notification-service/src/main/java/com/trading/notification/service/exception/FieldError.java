package com.trading.notification.service.exception;

/**
 * Detail of a single invalid field in a request.
 *
 * @param field   the name of the invalid field. Cannot be null or blank.
 * @param message the validation message for the field. Cannot be null or blank.
 */
public record FieldError(String field, String message) {
}
