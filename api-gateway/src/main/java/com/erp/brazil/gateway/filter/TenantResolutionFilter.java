package com.erp.brazil.gateway.filter;

import com.erp.brazil.common.domain.model.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Tenant Resolution Filter for API Gateway.
 * Resolves tenant ID from X-Tenant-ID header or JWT claims.
 * Ensures tenant context is available for downstream services.
 */
@Slf4j
@Component
public class TenantResolutionFilter implements GlobalFilter, Ordered {

    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String TENANT_CLAIM_HEADER = "X-Tenant-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip tenant resolution for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Try to get tenant from header (set by JWT filter or direct header)
        String tenantId = request.getHeaders().getFirst(TENANT_HEADER);

        // Fallback to X-Tenant-ID from JWT claims header
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getHeaders().getFirst(TENANT_CLAIM_HEADER);
        }

        if (!StringUtils.hasText(tenantId)) {
            log.warn("Missing tenant ID for path: {} from IP: {}", path, getClientIp(request));
            return tenantRequiredResponse(exchange);
        }

        // Validate tenant ID format (UUID or alphanumeric)
        if (!isValidTenantId(tenantId)) {
            log.warn("Invalid tenant ID format: {} for path: {}", tenantId, path);
            return invalidTenantResponse(exchange);
        }

        // Set tenant context
        TenantContext.setTenantId(tenantId);

        // Add tenant ID to request headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TENANT_HEADER, tenantId)
                .build();

        log.debug("Resolved tenant: {} for path: {}", tenantId, path);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> TenantContext.clear());
    }

    /**
     * Checks if the endpoint is public (no tenant required).
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/health") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/oauth/token") ||
                path.startsWith("/.well-known/");
    }

    /**
     * Validates tenant ID format.
     */
    private boolean isValidTenantId(String tenantId) {
        // Accept UUID format or alphanumeric with hyphens/underscores
        return tenantId.matches("^[a-zA-Z0-9_-]{1,100}$");
    }

    /**
     * Extracts client IP from request.
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * Returns 400 Bad Request for missing tenant.
     */
    private Mono<Void> tenantRequiredResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse().bufferFactory().wrap(
                        "{\"error\":\"Tenant Required\",\"message\":\"X-Tenant-ID header is required\"}".getBytes())));
    }

    /**
     * Returns 400 Bad Request for invalid tenant.
     */
    private Mono<Void> invalidTenantResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse().bufferFactory().wrap(
                        "{\"error\":\"Invalid Tenant\",\"message\":\"Invalid tenant ID format\"}".getBytes())));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}