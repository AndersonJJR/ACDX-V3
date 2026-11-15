package com.erp.brazil.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Rate Limiting Filter for API Gateway.
 * Implements token bucket algorithm using Redis for distributed rate limiting.
 * Limits are per tenant per endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // Default limits (can be configured per tenant/endpoint)
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 1000;
    private static final int DEFAULT_BURST_SIZE = 200;
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String USER_HEADER = "X-User-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip rate limiting for health endpoints
        if (isHealthEndpoint(path)) {
            return chain.filter(exchange);
        }

        String tenantId = request.getHeaders().getFirst(TENANT_HEADER);
        String userId = request.getHeaders().getFirst(USER_HEADER);

        if (!StringUtils.hasText(tenantId)) {
            // No tenant, skip rate limiting (will be caught by tenant filter)
            return chain.filter(exchange);
        }

        // Build rate limit key: ratelimit:{tenant}:{endpoint}:{minute}
        String endpointKey = normalizePath(path);
        String minuteBucket = String.valueOf(Instant.now().getEpochSecond() / 60);
        String rateLimitKey = RATE_LIMIT_PREFIX + tenantId + ":" + endpointKey + ":" + minuteBucket;

        // Use Redis Lua script for atomic check-and-increment
        String luaScript = """
                local current = redis.call('GET', KEYS[1])
                if current == false then
                    redis.call('SET', KEYS[1], 1, 'EX', 120)
                    return {1, tonumber(ARGV[1])}
                end
                if tonumber(current) >= tonumber(ARGV[1]) then
                    return {tonumber(current), tonumber(ARGV[1])}
                end
                local incremented = redis.call('INCR', KEYS[1])
                return {incremented, tonumber(ARGV[1])}
                """;

        return redisTemplate.execute(script -> script.eval(luaScript,
                        org.springframework.data.redis.core.script.RedisScript.RETURN_TYPE,
                        new String[]{rateLimitKey},
                        String.valueOf(DEFAULT_REQUESTS_PER_MINUTE)))
                .flatMap(result -> {
                    @SuppressWarnings("unchecked")
                    var list = (java.util.List<Long>) result;
                    long currentCount = list.get(0);
                    long limit = list.get(1);

                    // Add rate limit headers
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf((Instant.now().getEpochSecond() / 60 + 1) * 60));

                    if (currentCount > limit) {
                        log.warn("Rate limit exceeded for tenant: {}, endpoint: {}, count: {}/{}", tenantId, endpointKey, currentCount, limit);
                        return rateLimitExceededResponse(exchange, limit, currentCount);
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Rate limiting error: {}", e.getMessage());
                    // Fail open - allow request through if Redis is unavailable
                    return chain.filter(exchange);
                });
    }

    /**
     * Checks if the endpoint is a health check.
     */
    private boolean isHealthEndpoint(String path) {
        return path.startsWith("/health") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info");
    }

    /**
     * Normalizes path for rate limit key.
     */
    private String normalizePath(String path) {
        // Remove path parameters, keep only structure
        return path.replaceAll("/[0-9a-f-]{36}", "/:id")
                .replaceAll("/\\d+", "/:id")
                .replaceAll("[^a-zA-Z0-9:/_-]", "");
    }

    /**
     * Returns 429 Too Many Requests response.
     */
    private Mono<Void> rateLimitExceededResponse(ServerWebExchange exchange, long limit, long current) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Retry-After", "60");

        String body = String.format(
                "{\"error\":\"Rate Limit Exceeded\",\"message\":\"Too many requests\",\"limit\":%d,\"current\":%d,\"retryAfter\":60}",
                limit, current);

        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }
}