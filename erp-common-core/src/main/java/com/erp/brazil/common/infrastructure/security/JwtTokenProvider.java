package com.erp.brazil.common.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT token provider for creating and validating JSON Web Tokens.
 * Supports access tokens and refresh tokens with configurable expiration.
 */
@Slf4j
@Component
@Getter
@Setter
public class JwtTokenProvider {

    @Value("${app.jwt.secret:change-this-secret-in-production-min-256-bits}")
    private String secret;

    @Value("${app.jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    @Value("${app.jwt.refresh-token-validity-seconds:604800}")
    private long refreshTokenValiditySeconds;

    @Value("${app.jwt.issuer:erp-brazil}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            log.warn("JWT secret is too short or not configured. Using default for development only!");
            this.secret = "change-this-secret-in-production-min-256-bits-change-this-secret-in-production";
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates an access token for the given authentication.
     *
     * @param authentication the authentication
     * @return the JWT access token
     */
    public String createAccessToken(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .claim("userId", userDetails.getUserId())
                .claim("tenantId", userDetails.getTenantId())
                .claim("companyId", userDetails.getCompanyId())
                .claim("authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .claim("tokenType", "ACCESS")
                .signWith(key)
                .compact();
    }

    /**
     * Creates a refresh token for the given user details.
     *
     * @param userDetails the user details
     * @return the JWT refresh token
     */
    public String createRefreshToken(JwtUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .claim("userId", userDetails.getUserId())
                .claim("tenantId", userDetails.getTenantId())
                .claim("companyId", userDetails.getCompanyId())
                .claim("tokenType", "REFRESH")
                .signWith(key)
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses and validates a JWT token, returning the claims.
     *
     * @param token the JWT token
     * @return the parsed claims
     */
    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }

    /**
     * Gets the authentication from a valid JWT token.
     *
     * @param token the JWT token
     * @return the authentication
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String username = claims.getSubject();
        String userId = claims.get("userId", String.class);
        String tenantId = claims.get("tenantId", String.class);
        String companyId = claims.get("companyId", String.class);
        String authoritiesStr = claims.get("authorities", String.class);

        List<GrantedAuthority> authorities = Arrays.stream(authoritiesStr.split(","))
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .toList();

        JwtUserDetails userDetails = JwtUserDetails.builder()
                .username(username)
                .userId(userId)
                .tenantId(tenantId)
                .companyId(companyId)
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }

    /**
     * Gets the token type (ACCESS or REFRESH).
     *
     * @param token the JWT token
     * @return the token type
     */
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * Checks if the token is an access token.
     *
     * @param token the JWT token
     * @return true if access token
     */
    public boolean isAccessToken(String token) {
        return "ACCESS".equals(getTokenType(token));
    }

    /**
     * Checks if the token is a refresh token.
     *
     * @param token the JWT token
     * @return true if refresh token
     */
    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getTokenType(token));
    }

    /**
     * Gets the remaining validity in seconds.
     *
     * @param token the JWT token
     * @return remaining seconds
     */
    public long getRemainingValiditySeconds(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
}