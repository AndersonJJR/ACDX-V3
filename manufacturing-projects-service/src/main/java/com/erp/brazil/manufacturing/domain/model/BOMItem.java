package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bom_item", indexes = {
    @Index(name = "idx_bom_item_bom", columnList = "bom_id"),
    @Index(name = "idx_bom_item_component", columnList = "component_id"),
    @Index(name = "idx_bom_item_position", columnList = "position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BOMItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BOM bom;

    @NotNull
    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "component_type", length = 50)
    private String componentType;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "scrap_percentage", precision = 5, scale = 2)
    private BigDecimal scrapPercentage = BigDecimal.ZERO;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "is_phantom")
    private Boolean isPhantom = false;

    @Column(name = "is_optional")
    private Boolean isOptional = false;

    @Column(name = "reference_designator", length = 100)
    private String referenceDesignator;

    @Column(name = "find_number", length = 50)
    private String findNumber;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long versionLock;
}