package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import com.erp.brazil.common.domain.validation.ValidCnpj;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Company (Tenant) entity.
 * Represents a tenant in the multi-tenant system.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "companies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_cnpj", columnNames = "cnpj"),
        @UniqueConstraint(name = "uk_company_code", columnNames = "code")
}, indexes = {
        @Index(name = "idx_company_status", columnList = "status"),
        @Index(name = "idx_company_cnpj", columnList = "cnpj")
})
public class Company extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", length = 50, nullable = false, updatable = false)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 200)
    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @ValidCnpj
    @NotBlank
    @Column(name = "cnpj", length = 18, nullable = false, unique = true)
    private String cnpj;

    @Size(max = 20)
    @Column(name = "state_registration", length = 20)
    private String stateRegistration;

    @Size(max = 20)
    @Column(name = "municipal_registration", length = 20)
    private String municipalRegistration;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 2)
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 10)
    @Column(name = "cep", length = 10)
    private String cep;

    @Size(max = 100)
    @Column(name = "phone", length = 100)
    private String phone;

    @Email
    @Size(max = 200)
    @Column(name = "email", length = 200)
    private String email;

    @Size(max = 200)
    @Column(name = "website", length = 200)
    private String website;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Size(max = 1000)
    @Column(name = "settings", length = 1000)
    private String settings; // JSON configuration

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    public Company() {
        super();
    }

    public enum CompanyStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        TRIAL,
        CANCELLED
    }

    public boolean isActive() {
        return this.status == CompanyStatus.ACTIVE;
    }

    public void activate() {
        this.status = CompanyStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = CompanyStatus.INACTIVE;
    }

    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
    }
}