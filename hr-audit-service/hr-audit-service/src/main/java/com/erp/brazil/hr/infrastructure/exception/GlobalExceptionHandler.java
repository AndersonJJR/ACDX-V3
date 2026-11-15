package com.erp.brazil.hr.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String BASE_URI = "https://erp-brazil.com/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "validation-error"))
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed")
                .instance(URI.create(request.getRequestURI()))
                .property("errors", errors)
                .property("timestamp", Instant.now())
                .build();

        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "constraint-violation"))
                .title("Constraint Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Constraint validation failed")
                .instance(URI.create(request.getRequestURI()))
                .property("errors", errors)
                .property("timestamp", Instant.now())
                .build();

        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "malformed-request"))
                .title("Malformed Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request body is malformed or missing")
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.warn("Malformed request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "type-mismatch"))
                .title("Type Mismatch")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Parameter '" + ex.getName() + "' should be of type " + 
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"))
                .instance(URI.create(request.getRequestURI()))
                .property("parameter", ex.getName())
                .property("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getName() : "unknown")
                .property("timestamp", Instant.now())
                .build();

        log.warn("Type mismatch: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "not-found"))
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.debug("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "invalid-argument"))
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "illegal-state"))
                .title("Illegal State")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "access-denied"))
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have permission to access this resource")
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "unauthorized"))
                .title("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("Authentication required or failed")
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "http-error"))
                .title(ex.getReason())
                .status(ex.getStatusCode().value())
                .detail(ex.getReason())
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error: ", ex);
        
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create(BASE_URI + "internal-error"))
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please contact support.")
                .instance(URI.create(request.getRequestURI()))
                .property("timestamp", Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}