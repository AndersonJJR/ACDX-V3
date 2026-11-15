package com.erp.brazil.identity.presentation.controller;

import com.erp.brazil.common.application.response.ProblemDetail;
import com.erp.brazil.common.presentation.controller.BaseController;
import com.erp.brazil.identity.application.service.AuthService;
import com.erp.brazil.identity.presentation.request.LoginRequest;
import com.erp.brazil.identity.presentation.request.RefreshTokenRequest;
import com.erp.brazil.identity.presentation.request.ForgotPasswordRequest;
import com.erp.brazil.identity.presentation.request.ResetPasswordRequest;
import com.erp.brazil.identity.presentation.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller.
 * Handles login, token refresh, logout, and password reset.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access/refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getUsername());
        String clientIp = getClientIp(httpRequest);
        AuthResponse response = authService.login(request, clientIp);
        return ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate refresh token")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request");
        String token = extractToken(authHeader);
        authService.logout(token);
        return noContent();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Send password reset email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        authService.forgotPassword(request.getEmail());
        return noContent();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token from email")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset with token");
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return noContent();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                                @RequestParam String currentPassword,
                                                @RequestParam String newPassword) {
        String token = extractToken(authHeader);
        String userId = getUserId();
        authService.changePassword(userId, currentPassword, newPassword);
        return noContent();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if current token is valid")
    public ResponseEntity<AuthResponse.TokenValidation> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        AuthResponse.TokenValidation validation = authService.validateToken(token);
        return ok(validation);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}