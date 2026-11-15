package com.erp.brazil.common.presentation.exception;

import com.erp.brazil.common.application.response.ProblemDetail;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Global exception handler for REST APIs.
 * Converts exceptions to RFC 7807 ProblemDetail responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> violations = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            violations.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.validationError(
                "Request validation failed",
                ex.getParameter().getExecutable().toString(),
                violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> violations = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                violations.put(v.getPropertyPath().toString(), v.getMessage()));

        ProblemDetail problem = ProblemDetail.validationError(
                "Validation failed",
                "/",
                violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles entity not found exceptions.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/resource-not-found"))
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles data integrity violations (unique constraints, foreign keys).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        String detail = ex.getMostSpecificCause().getMessage();
        if (detail == null) {
            detail = "Data integrity violation";
        }

        // Check for common constraint violations
        String title = "Data Integrity Violation";
        if (detail.contains("unique") || detail.contains("Duplicate")) {
            title = "Duplicate Resource";
        } else if (detail.contains("foreign key")) {
            title = "Referenced Resource Not Found";
        }

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/data-integrity"))
                .title(title)
                .status(HttpStatus.CONFLICT.value())
                .detail(detail)
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing parameter: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/missing-parameter"))
                .title("Missing Parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Required parameter '" + ex.getParameterName() + "' is missing")
                .instance("/")
                .extensions(Map.of("parameter", (Object) ex.getParameterName()))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles method argument type mismatch.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/type-mismatch"))
                .title("Type Mismatch")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Parameter '" + ex.getName() + "' should be of type " + expectedType)
                .instance("/")
                .extensions(Map.of("parameter", (Object) ex.getName(), "expectedType", (Object) expectedType))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles HTTP message not readable (malformed JSON).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/malformed-request"))
                .title("Malformed Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request body is malformed or missing")
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles authentication failures.
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.unauthorized(
                "Authentication failed: " + ex.getMessage(),
                "/");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    /**
     * Handles access denied (authorization failures).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forbidden(
                "Access denied: " + ex.getMessage(),
                "/");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * Handles resource not found (404).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/endpoint-not-found"))
                .title("Endpoint Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail("The requested endpoint does not exist")
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ProblemDetail problem = ProblemDetail.internalError(
                "An unexpected error occurred",
                "/");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}