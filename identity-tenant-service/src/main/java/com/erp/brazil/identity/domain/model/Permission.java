package com.erp.brazil.identity.domain.model;

import com.erp.brazil.common.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Permission entity.
 * Represents a fine-grained permission for resources and actions.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_tenant_code", columnNames = {"tenant_id", "code"})
}, indexes = {
        @Index(name = "idx_permission_resource", columnList = "resource"),
        @Index(name = "idx_permission_action", columnList = "action")
})
public class Permission extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    @Column(name = "tenant_id", length = 100, nullable = false, updatable = false)
    private String tenantId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 200)
    @Column(name = "resource", length = 200, nullable = false)
    private String resource;

    @Size(max = 50)
    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    public Permission() {
        super();
    }

    public String getFullPermission() {
        return resource + ":" + action;
    }
}