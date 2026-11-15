package com.erp.brazil.common.infrastructure.security;

import com.erp.brazil.common.domain.model.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter for extracting and validating tokens from requests.
 * Also resolves tenant context from X-Tenant-ID header or token claims.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String TENANT_CLAIM = "tenantId";
    public static final String USER_ID_CLAIM = "userId";
    public static final String COMPANY_ID_CLAIM = "companyId";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                if (jwtTokenProvider.isAccessToken(jwt)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set tenant context from token claims
                    setTenantContextFromToken(jwt);
                }
            }

            // Override tenant from header if present (for service-to-service calls)
            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (StringUtils.hasText(tenantHeader)) {
                TenantContext.setTenantId(tenantHeader);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            // Don't block the request, let the controller handle authorization
            filterChain.doFilter(request, response);
        } finally {
            // Clear context after request to prevent leakage
            TenantContext.clear();
        }
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the token or null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Sets tenant context from JWT token claims.
     *
     * @param token the JWT token
     */
    private void setTenantContextFromToken(String token) {
        try {
            var claims = jwtTokenProvider.parseClaims(token);
            String tenantId = claims.get(TENANT_CLAIM, String.class);
            String userId = claims.get(USER_ID_CLAIM, String.class);
            String companyId = claims.get(COMPANY_ID_CLAIM, String.class);

            if (StringUtils.hasText(tenantId)) {
                TenantContext.setTenantId(tenantId);
            }
            if (StringUtils.hasText(userId)) {
                TenantContext.setUserId(userId);
            }
            if (StringUtils.hasText(companyId)) {
                TenantContext.setCompanyId(companyId);
            }
        } catch (Exception e) {
            log.debug("Failed to extract tenant context from token: {}", e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for public endpoints
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/login") ||
                path.equals("/oauth/token") ||
                path.startsWith("/.well-known/");
    }
}