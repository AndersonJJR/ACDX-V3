package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "code"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    @Column(name = "aisle", length = 20)
    private String aisle;

    @Column(name = "rack", length = 20)
    private String rack;

    @Column(name = "shelf", length = 20)
    private String shelf;

    @Column(name = "bin", length = 20)
    private String bin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_pickable", nullable = false)
    private Boolean isPickable = true;

    @Column(name = "is_receivable", nullable = false)
    private Boolean isReceivable = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public String getAisle() { return aisle; }
    public void setAisle(String aisle) { this.aisle = aisle; }

    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public String getBin() { return bin; }
    public void setBin(String bin) { this.bin = bin; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsPickable() { return isPickable; }
    public void setIsPickable(Boolean isPickable) { this.isPickable = isPickable; }

    public Boolean getIsReceivable() { return isReceivable; }
    public void setIsReceivable(Boolean isReceivable) { this.isReceivable = isReceivable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum LocationType {
        RECEIVING, STORAGE, PICKING, PACKING, SHIPPING, QUARANTINE, DAMAGED, TRANSIT
    }
}