package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity.
 * Represents a role with a set of permissions.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_tenant_code", columnNames = {"tenant_id", "code"})
}, indexes = {
        @Index(name = "idx_role_company", columnList = "company_id"),
        @Index(name = "idx_role_type", columnList = "role_type")
})
public class Role extends BaseEntity<String> implements Serializable {

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
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 20, nullable = false)
    private RoleType roleType = RoleType.CUSTOM;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
        super();
    }

    public enum RoleType {
        SYSTEM,
        ADMIN,
        MANAGER,
        OPERATOR,
        VIEWER,
        CUSTOM
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
}