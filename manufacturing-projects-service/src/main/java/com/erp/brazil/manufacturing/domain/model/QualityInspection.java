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
@Table(name = "quality_inspection", indexes = {
    @Index(name = "idx_qi_number", columnList = "inspection_number", unique = true),
    @Index(name = "idx_qi_type", columnList = "inspection_type"),
    @Index(name = "idx_qi_status", columnList = "status"),
    @Index(name = "idx_qi_product", columnList = "product_id"),
    @Index(name = "idx_qi_lot", columnList = "lot_number"),
    @Index(name = "idx_qi_dates", columnList = "inspection_date"),
    @Index(name = "idx_qi_order", columnList = "production_order_id"),
    @Index(name = "idx_qi_receipt", columnList = "goods_receipt_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "inspection_number", nullable = false, unique = true, length = 50)
    private String inspectionNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false, length = 30)
    private InspectionType inspectionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InspectionStatus status = InspectionStatus.PENDING;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "production_order_id")
    private UUID productionOrderId;

    @Column(name = "production_order_operation_id")
    private UUID productionOrderOperationId;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "goods_receipt_id")
    private UUID goodsReceiptId;

    @Column(name = "subcontracting_receipt_id")
    private UUID subcontractingReceiptId;

    @Column(name = "sampling_plan_id")
    private UUID samplingPlanId;

    @Column(name = "inspector_id", length = 100)
    private String inspectorId;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "accepted_quantity", precision = 19, scale = 6)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", precision = 19, scale = 6)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", length = 30)
    private Disposition disposition;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "qualityInspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QualityInspectionResult> results = new ArrayList<>();

    @OneToMany(mappedBy = "qualityInspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<NonConformance> nonConformances = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Version
    private Long versionLock;

    public enum InspectionType {
        INCOMING,
        IN_PROCESS,
        FINAL,
        FIRST_ARTICLE,
        PATROL,
        REWORK,
        SUBCONTRACTING,
        CUSTOMER_RETURN,
        VENDOR_AUDIT
    }

    public enum InspectionStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        PASSED,
        FAILED,
        CANCELLED
    }

    public enum Disposition {
        ACCEPT,
        REJECT,
        REWORK,
        USE_AS_IS,
        RETURN_TO_VENDOR,
        SCRAP,
        CONCESSION
    }

    public void addResult(QualityInspectionResult result) {
        results.add(result);
        result.setQualityInspection(this);
    }

    public void removeResult(QualityInspectionResult result) {
        results.remove(result);
        result.setQualityInspection(null);
    }

    public void addNonConformance(NonConformance nonConformance) {
        nonConformances.add(nonConformance);
        nonConformance.setQualityInspection(this);
    }

    public void removeNonConformance(NonConformance nonConformance) {
        nonConformances.remove(nonConformance);
        nonConformance.setQualityInspection(null);
    }
}