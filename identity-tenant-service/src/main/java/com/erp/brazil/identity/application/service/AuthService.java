package com.erp.brazil.identity.application.service;

import com.erp.brazil.common.infrastructure.security.JwtTokenProvider;
import com.erp.brazil.common.infrastructure.security.JwtUserDetails;
import com.erp.brazil.identity.domain.model.User;
import com.erp.brazil.identity.infrastructure.persistence.jpa.UserJpaRepository;
import com.erp.brazil.identity.infrastructure.security.UserDetailsServiceImpl;
import com.erp.brazil.identity.presentation.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service.
 * Handles login, token management, password operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserJpaRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(com.erp.brazil.identity.presentation.request.LoginRequest request, String clientIp) {
        log.debug("Authenticating user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("User is not active");
        }

        if (user.isLocked()) {
            throw new BadCredentialsException("User is locked");
        }

        // Record successful login
        user.recordLogin(clientIp);
        userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        Set<String> roles = permissions.stream()
                .filter(p -> p.startsWith("ROLE_"))
                .map(p -> p.substring(5))
                .collect(Collectors.toSet());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .companyId(user.getCompanyId())
                .avatarUrl(user.getAvatarUrl())
                .language(user.getLanguage())
                .timezone(user.getTimezone())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtTokenProvider.getAccessTokenValiditySeconds()))
                .user(userInfo)
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var claims = jwtTokenProvider.parseClaims(refreshToken);
        String userId = claims.get("userId", String.class);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("User is not active");
        }

        UserDetailsImpl userDetails = userDetailsService.loadUserById(userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        Set<String> roles = permissions.stream()
                .filter(p -> p.startsWith("ROLE_"))
                .map(p -> p.substring(5))
                .collect(Collectors.toSet());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .companyId(user.getCompanyId())
                .avatarUrl(user.getAvatarUrl())
                .language(user.getLanguage())
                .timezone(user.getTimezone())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .build();

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtTokenProvider.getAccessTokenValiditySeconds()))
                .user(userInfo)
                .permissions(permissions)
                .roles(roles)
                .build();
    }

    public void logout(String accessToken) {
        // In a stateless JWT system, logout is typically client-side
        // For server-side invalidation, we would need a token blacklist (Redis)
        log.debug("Logout requested");
    }

    public void forgotPassword(String email) {
        // Generate reset token, send email
        // Implementation would use a password reset token stored in Redis/DB
        log.info("Password reset requested for email: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        // Validate token, update password
        log.info("Password reset with token");
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(OffsetDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    public AuthResponse.TokenValidation validateToken(String token) {
        boolean valid = jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token);
        
        if (!valid) {
            return AuthResponse.TokenValidation.builder()
                    .valid(false)
                    .build();
        }

        var claims = jwtTokenProvider.parseClaims(token);
        long remaining = jwtTokenProvider.getRemainingValiditySeconds(token);

        return AuthResponse.TokenValidation.builder()
                .valid(true)
                .expiresIn(remaining)
                .expiresAt(OffsetDateTime.now().plusSeconds(remaining))
                .userId(claims.get("userId", String.class))
                .tenantId(claims.get("tenantId", String.class))
                .companyId(claims.get("companyId", String.class))
                .build();
    }
}