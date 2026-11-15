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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
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

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getMrNo() { return mrNo; }
    public void setMrNo(String mrNo) { this.mrNo = mrNo; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public LocalDate getRequiredDate() { return requiredDate; }
    public void setRequiredDate(LocalDate requiredDate) { this.requiredDate = requiredDate; }

    public MRStatus getStatus() { return status; }
    public void setStatus(MRStatus status) { this.status = status; }

    public Purpose getPurpose() { return purpose; }
    public void setPurpose(Purpose purpose) { this.purpose = purpose; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(UUID productionOrderId) { this.productionOrderId = productionOrderId; }

    public UUID getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(UUID salesOrderId) { this.salesOrderId = salesOrderId; }

    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum MRStatus {
        DRAFT, SUBMITTED, APPROVED, PARTIALLY_ISSUED, ISSUED, TRANSFERRED, COMPLETED, CANCELLED, ON_HOLD
    }

    public enum Purpose {
        MANUFACTURE, SALES, REPACK, SUB_CONTRACT, MATERIAL_TRANSFER, OTHER
    }
}