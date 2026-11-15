package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import com.erp.brazil.common.domain.validation.ValidCpf;
import com.erp.brazil.common.domain.validation.ValidPhone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * User entity.
 * Represents a system user with authentication and profile information.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = {"tenant_id", "username"}),
        @UniqueConstraint(name = "uk_user_email", columnNames = {"tenant_id", "email"}),
        @UniqueConstraint(name = "uk_user_cpf", columnNames = {"tenant_id", "cpf"})
}, indexes = {
        @Index(name = "idx_user_company", columnList = "company_id"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_email", columnList = "email")
})
public class User extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    @Column(name = "tenant_id", length = 100, nullable = false, updatable = false)
    private String tenantId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "company_id", length = 100, nullable = false, updatable = false)
    private String companyId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 200)
    @Column(name = "password", length = 200, nullable = false)
    private String password;

    @NotBlank
    @Size(max = 200)
    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @ValidCpf
    @Size(max = 14)
    @Column(name = "cpf", length = 14)
    private String cpf;

    @Email
    @Size(max = 200)
    @Column(name = "email", length = 200, nullable = false)
    private String email;

    @ValidPhone
    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @ValidPhone
    @Size(max = 20)
    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Size(max = 100)
    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Size(max = 1000)
    @Column(name = "preferences", length = 1000)
    private String preferences; // JSON

    @Size(max = 100)
    @Column(name = "language", length = 100)
    private String language = "pt-BR";

    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    private String timezone = "America/Sao_Paulo";

    public User() {
        super();
    }

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED,
        PENDING_VERIFICATION,
        SUSPENDED
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == UserStatus.LOCKED ||
                (this.lockedUntil != null && this.lockedUntil.isAfter(OffsetDateTime.now()));
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void lock(OffsetDateTime until) {
        this.status = UserStatus.LOCKED;
        this.lockedUntil = until;
    }

    public void incrementFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= 5) {
            this.lock(OffsetDateTime.now().plusMinutes(30));
        }
    }

    public void resetFailedLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void recordLogin(String ip) {
        this.lastLoginAt = OffsetDateTime.now();
        this.lastLoginIp = ip;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
}