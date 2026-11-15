package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter
import lombok.ToString;

import java.io.Serializable;

/**
 * Branch (Filial) entity.
 * Represents a physical branch/location of a company.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "branches", uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_tenant_code", columnNames = {"tenant_id", "company_id", "code"}),
        @UniqueConstraint(name = "uk_branch_tenant_cnpj", columnNames = {"tenant_id", "cnpj"})
}, indexes = {
        @Index(name = "idx_branch_company", columnList = "company_id"),
        @Index(name = "idx_branch_status", columnList = "status"),
        @Index(name = "idx_branch_cnpj", columnList = "cnpj")
})
public class Branch extends BaseEntity<String> implements Serializable {

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
    @Size(max = 50)
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 200)
    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Size(max = 18)
    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Size(max = 20)
    @Column(name = "state_registration", length = 20)
    private String stateRegistration;

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

    @Size(max = 200)
    @Column(name = "email", length = 200)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BranchStatus status = BranchStatus.ACTIVE;

    @Column(name = "is_headquarters", nullable = false)
    private Boolean isHeadquarters = false;

    @Column(name = "opening_date")
    private java.time.LocalDate openingDate;

    @Size(max = 1000)
    @Column(name = "settings", length = 1000)
    private String settings; // JSON

    public Branch() {
        super();
    }

    public enum BranchStatus {
        ACTIVE,
        INACTIVE,
        CLOSED,
        UNDER_CONSTRUCTION
    }

    public boolean isActive() {
        return this.status == BranchStatus.ACTIVE;
    }
}