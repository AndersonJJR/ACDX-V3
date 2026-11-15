package com.erp.brazil.common.presentation.controller;

import com.erp.brazil.common.application.response.PageableResponse;
import com.erp.brazil.common.application.response.CursorPage;
import com.erp.brazil.common.application.response.ProblemDetail;
import com.erp.brazil.common.domain.model.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

/**
 * Base controller providing common functionality for all REST controllers.
 * Includes tenant context handling, standardized responses, and error handling.
 */
@Slf4j
public abstract class BaseController {

    /**
     * Gets the current tenant ID from context.
     *
     * @return the tenant ID
     */
    protected String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }

    /**
     * Gets the current user ID from context.
     *
     * @return the user ID
     */
    protected String getUserId() {
        return TenantContext.getUserId();
    }

    /**
     * Gets the current company ID from context.
     *
     * @return the company ID
     */
    protected String getCompanyId() {
        return TenantContext.getCompanyId();
    }

    /**
     * Creates a successful response with body.
     *
     * @param body the response body
     * @param <T> the body type
     * @return response entity
     */
    protected <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Creates a successful response with no body.
     *
     * @return response entity
     */
    protected ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }

    /**
     * Creates a created response with location header.
     *
     * @param location the resource location
     * @param body the response body
     * @param <T> the body type
     * @return response entity
     */
    protected <T> ResponseEntity<T> created(URI location, T body) {
        return ResponseEntity.created(location).body(body);
    }

    /**
     * Creates a created response with location header (string).
     *
     * @param location the resource location
     * @param body the response body
     * @param <T> the body type
     * @return response entity
     */
    protected <T> ResponseEntity<T> created(String location, T body) {
        return ResponseEntity.created(URI.create(location)).body(body);
    }

    /**
     * Creates a no content response.
     *
     * @return response entity
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates an accepted response for async operations.
     *
     * @param body the response body
     * @param <T> the body type
     * @return response entity
     */
    protected <T> ResponseEntity<T> accepted(T body) {
        return ResponseEntity.accepted().body(body);
    }

    /**
     * Creates a paginated response.
     *
     * @param response the pageable response
     * @param <T> the content type
     * @return response entity
     */
    protected <T> ResponseEntity<PageableResponse<T>> paged(PageableResponse<T> response) {
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a cursor-paginated response.
     *
     * @param response the cursor page response
     * @param <T> the content type
     * @return response entity
     */
    protected <T> ResponseEntity<CursorPage<T>> cursorPaged(CursorPage<T> response) {
        return ResponseEntity.ok(response);
    }

    /**
     * Handles validation exceptions.
     *
     * @param ex the exception
     * @return problem detail response
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    protected ResponseEntity<ProblemDetail> handleValidationException(
            jakarta.validation.ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        var violations = ex.getConstraintViolations().stream()
                .collect(java.util.stream.Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        jakarta.validation.ConstraintViolation::getMessage,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new));

        ProblemDetail problem = ProblemDetail.validationError(
                "Validation failed",
                "/",
                violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the exception
     * @return problem detail response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/invalid-argument"))
                .title("Invalid Argument")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles illegal state exceptions.
     *
     * @param ex the exception
     * @return problem detail response
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/illegal-state"))
                .title("Illegal State")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance("/")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}