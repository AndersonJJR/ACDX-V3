package com.erp.brazil.identity.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Authentication response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private OffsetDateTime expiresAt;
    private UserInfo user;
    private Set<String> permissions;
    private Set<String> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String userId;
        private String username;
        private String fullName;
        private String email;
        private String tenantId;
        private String companyId;
        private String avatarUrl;
        private String language;
        private String timezone;
        private Boolean twoFactorEnabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidation {
        private boolean valid;
        private Long expiresIn;
        private OffsetDateTime expiresAt;
        private String userId;
        private String tenantId;
        private String companyId;
    }
}