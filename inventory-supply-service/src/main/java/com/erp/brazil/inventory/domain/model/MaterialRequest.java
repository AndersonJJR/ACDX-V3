package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "material_requests", indexes = {
    @Index(name = "idx_mr_company_no", columnList = "company_id, mr_no", unique = true),
    @Index(name = "idx_mr_status", columnList = "status"),
    @Index(name = "idx_mr_date", columnList = "request_date"),
    @Index(name = "idx_mr_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_mr_production_order", columnList = "production_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "mr_no", nullable = false, length = 50)
    private String mrNo;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MRStatus status = MRStatus.DRAFT;

    @Column(name = "purpose", length = 20)
    @Enumerated(EnumType.STRING)
    private Purpose purpose;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "production_order_id")
    private UUID productionOrderId;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "total_qty", precision = 20, scale = 6)
    private BigDecimal totalQty;

    @Column(name = "total_amount", precision = 20, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum MRStatus {
        DRAFT, SUBMITTED, APPROVED, PARTIALLY_ISSUED, ISSUED, TRANSFERRED, COMPLETED, CANCELLED, ON_HOLD
    }

    public enum Purpose {
        MANUFACTURE, SALES, REPACK, SUB_CONTRACT, MATERIAL_TRANSFER, OTHER
    }
}