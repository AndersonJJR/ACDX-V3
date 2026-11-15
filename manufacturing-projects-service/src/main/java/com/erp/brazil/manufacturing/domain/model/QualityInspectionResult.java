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
@Table(name = "quality_inspection_result", indexes = {
    @Index(name = "idx_qir_inspection", columnList = "quality_inspection_id"),
    @Index(name = "idx_qir_characteristic", columnList = "characteristic_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityInspectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_inspection_id", nullable = false)
    private QualityInspection qualityInspection;

    @NotNull
    @Column(name = "characteristic_id", nullable = false)
    private UUID characteristicId;

    @Column(name = "characteristic_name", length = 200)
    private String characteristicName;

    @Column(name = "specification_min", precision = 19, scale = 6)
    private BigDecimal specificationMin;

    @Column(name = "specification_max", precision = 19, scale = 6)
    private BigDecimal specificationMax;

    @Column(name = "specification_target", precision = 19, scale = 6)
    private BigDecimal specificationTarget;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "measured_value", precision = 19, scale = 6)
    private BigDecimal measuredValue;

    @Column(name = "measured_values", columnDefinition = "jsonb")
    private String measuredValues;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    private Result result;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long versionLock;

    public enum Result {
        PASS,
        FAIL,
        MARGINAL
    }
}