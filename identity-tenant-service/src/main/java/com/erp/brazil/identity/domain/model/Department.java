package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index
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
 * Department entity.
 * Represents an organizational department within a company/branch.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_department_tenant_code", columnNames = {"tenant_id", "company_id", "branch_id", "code"})
}, indexes = {
        @Index(name = "idx_department_company", columnList = "company_id"),
        @Index(name = "idx_department_branch", columnList = "branch_id"),
        @Index(name = "idx_department_parent", columnList = "parent_id"),
        @Index(name = "idx_department_status", columnList = "status")
})
public class Department extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    @Column(name = "tenant_id", length = 100, nullable = false, updatable = false)
    private String tenantId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "company_id", length = 100, nullable = false, updatable = false)
    private String companyId;

    @Size(max = 100)
    @Column(name = "branch_id", length = 100)
    private String branchId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 100)
    @Column(name = "parent_id", length = 100)
    private String parentId;

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 100)
    @Column(name = "manager_id", length = 100)
    private String managerId;

    @Size(max = 1000)
    @Column(name = "settings", length = 1000)
    private String settings; // JSON

    public Department() {
        super();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }
}