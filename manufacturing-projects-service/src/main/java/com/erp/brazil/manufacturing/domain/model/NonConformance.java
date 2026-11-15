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
@Table(name = "non_conformance", indexes = {
    @Index(name = "idx_nc_number", columnList = "nc_number", unique = true),
    @Index(name = "idx_nc_inspection", columnList = "quality_inspection_id"),
    @Index(name = "idx_nc_product", columnList = "product_id"),
    @Index(name = "idx_nc_status", columnList = "status"),
    @Index(name = "idx_nc_severity", columnList = "severity"),
    @Index(name = "idx_nc_assignee", columnList = "assigned_to"),
    @Index(name = "idx_nc_dates", columnList = "identified_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonConformance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "nc_number", nullable = false, unique = true, length = 50)
    private String ncNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id")
    private QualityInspection qualityInspection;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "production_order_id")
    private UUID productionOrderId;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NCStatus status = NCStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30)
    private Source source;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "root_cause", length = 2000)
    private String rootCause;

    @Column(name = "identified_by", length = 100)
    private String identifiedBy;

    @Column(name = "identified_date", nullable = false)
    private LocalDateTime identifiedDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "target_resolution_date")
    private LocalDateTime targetResolutionDate;

    @Column(name = "actual_resolution_date")
    private LocalDateTime actualResolutionDate;

    @Column(name = "quantity_affected", precision = 19, scale = 6)
    private BigDecimal quantityAffected = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "cost_impact", precision = 19, scale = 4)
    private BigDecimal costImpact = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "nonConformance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CorrectiveAction> correctiveActions = new ArrayList<>();

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

    public enum Severity {
        CRITICAL,
        MAJOR,
        MINOR,
        OBSERVATION
    }

    public enum NCStatus {
        OPEN,
        UNDER_INVESTIGATION,
        ROOT_CAUSE_IDENTIFIED,
        CORRECTIVE_ACTION_PLANNED,
        CORRECTIVE_ACTION_IN_PROGRESS,
        CORRECTIVE_ACTION_COMPLETED,
        VERIFICATION_PENDING,
        VERIFIED,
        CLOSED,
        CANCELLED
    }

    public enum Source {
        INTERNAL,
        CUSTOMER_COMPLAINT,
        SUPPLIER,
        AUDIT,
        REGULATORY,
        PRODUCTION,
        INSPECTION
    }

    public void addCorrectiveAction(CorrectiveAction action) {
        correctiveActions.add(action);
        action.setNonConformance(this);
    }

    public void removeCorrectiveAction(CorrectiveAction action) {
        correctiveActions.remove(action);
        action.setNonConformance(null);
    }
}