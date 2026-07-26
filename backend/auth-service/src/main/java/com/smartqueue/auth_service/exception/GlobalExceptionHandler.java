package com.smartqueue.auth_service.exception;

import com.smartqueue.auth_service.constant.SecurityConstants;
import com.smartqueue.auth_service.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
    }

    @ExceptionHandler({UnauthorizedException.class, InvalidTokenException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            Exception ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", "Access is denied", request, null);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            AuthException ex, HttpServletRequest request) {
        log.warn("Authentication domain exception: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request parameters", request, errors);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error occurred on path {}: ", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected internal error occurred. Please contact system administrator.", request, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        String correlationId = MDC.get(SecurityConstants.CORRELATION_ID_LOG_VAR);

        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(response, status);
    }
}
