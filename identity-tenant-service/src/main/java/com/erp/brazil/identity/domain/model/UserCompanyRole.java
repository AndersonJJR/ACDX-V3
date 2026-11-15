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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * UserCompanyRole entity.
 * Maps users to roles within a specific company/branch context.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "user_company_roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_company_role", columnNames = {"tenant_id", "user_id", "company_id", "role_id", "branch_id"})
}, indexes = {
        @Index(name = "idx_ucr_user", columnList = "user_id"),
        @Index(name = "idx_ucr_company", columnList = "company_id"),
        @Index(name = "idx_ucr_role", columnList = "role_id"),
        @Index(name = "idx_ucr_branch", columnList = "branch_id"),
        @Index(name = "idx_ucr_validity", columnList = "valid_from,valid_until")
})
public class UserCompanyRole extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    @Column(name = "tenant_id", length = 100, nullable = false, updatable = false)
    private String tenantId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "user_id", length = 100, nullable = false, updatable = false)
    private String userId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "company_id", length = 100, nullable = false, updatable = false)
    private String companyId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "role_id", length = 100, nullable = false, updatable = false)
    private String roleId;

    @Size(max = 100)
    @Column(name = "branch_id", length = 100)
    private String branchId;

    @Size(max = 100)
    @Column(name = "department_id", length = 100)
    private String departmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", length = 20, nullable = false)
    private AssignmentType assignmentType = AssignmentType.DIRECT;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Size(max = 100)
    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    public UserCompanyRole() {
        super();
        this.validFrom = OffsetDateTime.now();
    }

    public enum AssignmentType {
        DIRECT,
        GROUP,
        INHERITED,
        TEMPORARY
    }

    public boolean isValidAt(OffsetDateTime dateTime) {
        if (this.validFrom != null && dateTime.isBefore(this.validFrom)) {
            return false;
        }
        if (this.validUntil != null && dateTime.isAfter(this.validUntil)) {
            return false;
        }
        return true;
    }

    public boolean isCurrentlyValid() {
        return isValidAt(OffsetDateTime.now());
    }
}