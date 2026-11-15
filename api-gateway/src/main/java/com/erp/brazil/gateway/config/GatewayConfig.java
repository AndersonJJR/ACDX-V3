package com.erp.brazil.gateway.config;

import com.erp.brazil.gateway.filter.JwtAuthenticationFilter;
import com.erp.brazil.gateway.filter.TenantResolutionFilter;
import com.erp.brazil.gateway.filter.RateLimitingFilter;
import com.erp.brazil.gateway.handler.GlobalErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * API Gateway Configuration.
 * Defines routes for all 7 backend services with JWT validation,
 * tenant resolution, rate limiting, CORS, and circuit breaker.
 */
@Configuration
public class GatewayConfig {

    @Value("${gateway.cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Value("${gateway.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${gateway.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${gateway.cors.exposed-headers:X-Tenant-ID,X-Request-ID}")
    private List<String> exposedHeaders;

    @Value("${gateway.cors.max-age:3600}")
    private Long maxAge;

    /**
     * Defines all service routes with filters.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                            JwtAuthenticationFilter jwtFilter,
                                            TenantResolutionFilter tenantFilter,
                                            RateLimitingFilter rateLimitFilter) {
        return builder.routes()
                // Identity & Tenant Service
                .route("identity-service", r -> r
                        .path("/api/identity/**", "/api/tenants/**", "/api/companies/**", "/api/users/**",
                                "/api/roles/**", "/api/permissions/**", "/api/branches/**", "/api/departments/**",
                                "/api/cost-centers/**", "/api/numbering-series/**", "/api/auth/**", "/api/bootinfo/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("identity-service").setFallbackUri("forward:/fallback/identity"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://identity-tenant-service"))

                // Finance & Accounting Service
                .route("finance-service", r -> r
                        .path("/api/finance/**", "/api/accounting/**", "/api/fiscal/**", "/api/tax/**",
                                "/api/bank/**", "/api/payment/**", "/api/receivable/**", "/api/payable/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("finance-service").setFallbackUri("forward:/fallback/finance"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://finance-accounting-service"))

                // Fiscal Brazil Service
                .route("fiscal-service", r -> r
                        .path("/api/fiscal/**", "/api/nfe/**", "/api/nfce/**", "/api/cte/**", "/api/mdfe/**",
                                "/api/sped/**", "/api/efd/**", "/api/dfe/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("fiscal-service").setFallbackUri("forward:/fallback/fiscal"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://fiscal-brazil-service"))

                // HR & Audit Service
                .route("hr-service", r -> r
                        .path("/api/hr/**", "/api/payroll/**", "/api/employee/**", "/api/contract/**",
                                "/api/benefits/**", "/api/time/**", "/api/audit/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("hr-service").setFallbackUri("forward:/fallback/hr"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://hr-audit-service"))

                // Inventory & Supply Service
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/warehouse/**", "/api/product/**", "/api/stock/**",
                                "/api/purchase/**", "/api/supplier/**", "/api/supply/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("inventory-service").setFallbackUri("forward:/fallback/inventory"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://inventory-supply-service"))

                // Manufacturing & Projects Service
                .route("manufacturing-service", r -> r
                        .path("/api/manufacturing/**", "/api/production/**", "/api/bom/**", "/api/routing/**",
                                "/api/work-order/**", "/api/project/**", "/api/mrp/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("manufacturing-service").setFallbackUri("forward:/fallback/manufacturing"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://manufacturing-projects-service"))

                // Sales & CRM Service
                .route("sales-service", r -> r
                        .path("/api/sales/**", "/api/crm/**", "/api/customer/**", "/api/lead/**",
                                "/api/opportunity/**", "/api/quote/**", "/api/order/**", "/api/contract/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(c -> c.setName("sales-service").setFallbackUri("forward:/fallback/sales"))
                                .retry(c -> c.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.HEAD)
                                        .setBackoff(100, 200, 2)))
                        .uri("lb://sales-crm-service")

                // Actuator endpoints (internal only)
                .route("actuator", r -> r
                        .path("/actuator/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(tenantFilter))
                        .uri("lb://identity-tenant-service"))

                // Health check (no auth required)
                .route("health", r -> r
                        .path("/health", "/actuator/health", "/actuator/info")
                        .uri("lb://identity-tenant-service"))

                .build();
    }

    /**
     * CORS Configuration for the gateway.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setExposedHeaders(exposedHeaders);
        config.setMaxAge(maxAge);
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * Global error handler for gateway errors.
     */
    @Bean
    public GlobalErrorHandler globalErrorHandler() {
        return new GlobalErrorHandler();
    }
}