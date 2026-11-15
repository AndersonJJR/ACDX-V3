package com.erp.brazil.gateway.handler;

import com.erp.brazil.common.application.response.ProblemDetail;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Global Error Handler for API Gateway.
 * Converts all exceptions to RFC 7807 ProblemDetail responses.
 */
@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer codecConfigurer;

    public GlobalErrorHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer codecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.codecConfigurer = codecConfigurer;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineStatus(ex);
        ProblemDetail problem = buildProblemDetail(exchange, ex, status);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(BodyInserters.fromValue(problem))
                .flatMap(response -> response.writeTo(exchange, new ResponseContext()));
    }

    /**
     * Determines HTTP status based on exception type.
     */
    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof org.springframework.security.authentication.AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof jakarta.validation.ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof jakarta.persistence.EntityNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof java.util.NoSuchElementException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Builds ProblemDetail from exception.
     */
    private ProblemDetail buildProblemDetail(ServerWebExchange exchange, Throwable ex, HttpStatus status) {
        String path = exchange.getRequest().getPath().value();
        String message = ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();

        ProblemDetail.ProblemDetailBuilder builder = ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/" + status.name().toLowerCase()))
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(message)
                .instance(path);

        // Add extensions based on exception type
        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException ve) {
            var violations = ve.getBindingResult().getFieldErrors().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid",
                            (a, b) -> a,
                            java.util.LinkedHashMap::new));
            builder.extension("violations", violations);
        } else if (ex instanceof jakarta.validation.ConstraintViolationException cve) {
            var violations = cve.getConstraintViolations().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            jakarta.validation.ConstraintViolation::getMessage,
                            (a, b) -> a,
                            java.util.LinkedHashMap::new));
            builder.extension("violations", violations);
        }

        return builder.build();
    }

    /**
     * Response context for writing to exchange.
     */
    private static class ResponseContext implements ServerResponse.Context {
        @Override
        public List<ViewResolver> viewResolvers() {
            return Collections.emptyList();
        }

        @Override
        public ServerCodecConfigurer codecConfigurer() {
            return null; // Will be set by the handler
        }
    }
}