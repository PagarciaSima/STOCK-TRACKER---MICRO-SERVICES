package com.trading.user.service.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler for the user-service REST API.
 * <p>
 * Translates validation exceptions into standardized {@code 400 Bad Request}
 * responses with field-level detail, so clients receive actionable error
 * information instead of stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean validation failures on {@code @Valid} method arguments.
     *
     * @param ex the validation exception containing field errors. Cannot be null.
     * @return a {@code 400 Bad Request} response with an {@link ApiError} body
     *         listing each invalid field and its message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed",
                fieldErrors);
        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Handles constraint violations (e.g. on path variables or request parameters).
     *
     * @param ex the constraint violation exception. Cannot be null.
     * @return a {@code 400 Bad Request} response with an {@link ApiError} body.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new FieldError(
                        cv.getPropertyPath().toString(),
                        cv.getMessage()))
                .collect(Collectors.toList());
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "Request constraint violation",
                fieldErrors);
        return ResponseEntity.badRequest().body(apiError);
    }
}
