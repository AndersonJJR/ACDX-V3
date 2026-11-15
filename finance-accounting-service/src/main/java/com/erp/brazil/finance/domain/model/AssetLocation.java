package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "asset_locations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_asset_location_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_asset_location_name", columnNames = "name")
})
public class AssetLocation {

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

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDate createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public AssetLocation() {
        this.createdAt = java.time.LocalDate.now();
    }

    public AssetLocation(String code, String name) {
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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
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