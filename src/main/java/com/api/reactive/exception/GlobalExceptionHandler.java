package com.api.reactive.exception;

import com.api.reactive.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 Not Found ───────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, ServerWebExchange exchange) {

        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), exchange);
    }

    // ── 409 Conflict ────────────────────────────────────────────────────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex, ServerWebExchange exchange) {

        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), exchange);
    }

    // ── 400 Business rule violation ──────────────────────────────────────────────
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex, ServerWebExchange exchange) {

        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getStatus().getReasonPhrase().toUpperCase().replace(' ', '_'),
                ex.getMessage(), exchange);
    }

    // ── 400 Validation errors ────────────────────────────────────────────────────
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            WebExchangeBindException ex, ServerWebExchange exchange) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        log.warn("Validation failed: {}", errors);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request validation failed. See 'validationErrors' for details.")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400 Illegal argument ─────────────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, ServerWebExchange exchange) {

        log.warn("Illegal argument: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), exchange);
    }

    // ── 500 Unexpected ───────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, ServerWebExchange exchange) {

        log.error("Unexpected error on path {}: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support.", exchange);
    }

    // ── Helper ───────────────────────────────────────────────────────────────────
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String error, String message, ServerWebExchange exchange) {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
