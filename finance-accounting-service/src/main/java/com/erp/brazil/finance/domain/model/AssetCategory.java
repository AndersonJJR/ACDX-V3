package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "asset_categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_asset_category_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_asset_category_name", columnNames = "name")
})
public class AssetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "default_useful_life_months")
    private Integer defaultUsefulLifeMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_depreciation_method", length = 20)
    private DepreciationMethod defaultDepreciationMethod;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDate createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public AssetCategory() {
        this.createdAt = java.time.LocalDate.now();
    }

    public AssetCategory(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDefaultUsefulLifeMonths() { return defaultUsefulLifeMonths; }
    public void setDefaultUsefulLifeMonths(Integer defaultUsefulLifeMonths) { this.defaultUsefulLifeMonths = defaultUsefulLifeMonths; }
    public DepreciationMethod getDefaultDepreciationMethod() { return defaultDepreciationMethod; }
    public void setDefaultDepreciationMethod(DepreciationMethod defaultDepreciationMethod) { this.defaultDepreciationMethod = defaultDepreciationMethod; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public java.time.LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDate createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDate updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDate.now();
    }
}