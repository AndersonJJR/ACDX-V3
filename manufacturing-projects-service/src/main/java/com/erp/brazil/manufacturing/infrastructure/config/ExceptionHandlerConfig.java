package com.erp.brazil.manufacturing.infrastructure.config;

import com.erp.brazil.manufacturing.presentation.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerConfig {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/validation-error")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .validationErrors(errors)
                .build();

        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/constraint-violation")
                .title("Constraint Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Constraint validation failed")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/malformed-request")
                .title("Malformed Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request body is malformed or missing")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/missing-parameter")
                .title("Missing Parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Required parameter '" + ex.getParameterName() + "' is missing")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/type-mismatch")
                .title("Type Mismatch")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Parameter '" + ex.getName() + "' should be of type " + ex.getRequiredType().getSimpleName())
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/illegal-argument")
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/illegal-state")
                .title("Invalid State")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/access-denied")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You don't have permission to access this resource")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail("The requested resource was not found")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://erp-brazil.com/errors/internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred")
                .instance(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}