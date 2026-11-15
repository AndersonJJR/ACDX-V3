package com.erp.brazil.gateway.filter;

import com.erp.brazil.common.infrastructure.security.JwtTokenProvider;
import com.erp.brazil.common.domain.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter for API Gateway.
 * Validates JWT tokens and extracts user/tenant information for downstream services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or invalid Authorization header for path: {}", path);
            return unauthorizedResponse(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.debug("Invalid JWT token for path: {}", path);
                return unauthorizedResponse(exchange);
            }

            if (!jwtTokenProvider.isAccessToken(token)) {
                log.debug("Token is not an access token for path: {}", path);
                return unauthorizedResponse(exchange);
            }

            // Extract claims and add as headers for downstream services
            var claims = jwtTokenProvider.parseClaims(token);

            String userId = claims.get("userId", String.class);
            String tenantId = claims.get("tenantId", String.class);
            String companyId = claims.get("companyId", String.class);
            String authorities = claims.get("authorities", String.class);

            // Build mutated request with headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-ID", userId != null ? userId : "")
                    .header("X-Tenant-ID", tenantId != null ? tenantId : "")
                    .header("X-Company-ID", companyId != null ? companyId : "")
                    .header("X-Authorities", authorities != null ? authorities : "")
                    .build();

            // Set tenant context for logging/auditing
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }
            if (userId != null) {
                TenantContext.setUserId(userId);
            }
            if (companyId != null) {
                TenantContext.setCompanyId(companyId);
            }

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.debug("JWT validation failed for path {}: {}", path, e.getMessage());
            return unauthorizedResponse(exchange);
        } finally {
            // Clear context after request
            TenantContext.clear();
        }
    }

    /**
     * Checks if the endpoint is public (no authentication required).
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
     * Returns 401 Unauthorized response.
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"erp-brazil\"");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}