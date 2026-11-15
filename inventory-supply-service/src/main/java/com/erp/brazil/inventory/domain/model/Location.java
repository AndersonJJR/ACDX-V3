package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public enum LocationType {
        RECEIVING, STORAGE, PICKING, PACKING, SHIPPING, QUARANTINE, DAMAGED, TRANSIT
    }
}