package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routing", indexes = {
    @Index(name = "idx_routing_product", columnList = "product_id"),
    @Index(name = "idx_routing_status", columnList = "status"),
    @Index(name = "idx_routing_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RoutingType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoutingStatus status;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "base_quantity", precision = 19, scale = 6)
    private BigDecimal baseQuantity = BigDecimal.ONE;

    @Column(name = "total_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal totalSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "total_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal totalRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @OneToMany(mappedBy = "routing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoutingOperation> operations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long versionLock;

    public enum RoutingType {
        PRODUCTION,
        REWORK,
        PROTOTYPE,
        MAINTENANCE
    }

    public enum RoutingStatus {
        DRAFT,
        ACTIVE,
        OBSOLETE,
        ARCHIVED
    }

    public void addOperation(RoutingOperation operation) {
        operations.add(operation);
        operation.setRouting(this);
    }

    public void removeOperation(RoutingOperation operation) {
        operations.remove(operation);
        operation.setRouting(null);
    }

    public void recalculateTotals() {
        this.totalSetupTimeMinutes = operations.stream()
            .map(RoutingOperation::getSetupTimeMinutes)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalRunTimeMinutes = operations.stream()
            .map(RoutingOperation::getRunTimeMinutes)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}