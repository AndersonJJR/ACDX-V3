package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations", indexes = {
    @Index(name = "idx_reservation_item_warehouse", columnList = "item_id, warehouse_id"),
    @Index(name = "idx_reservation_voucher", columnList = "voucher_type, voucher_no"),
    @Index(name = "idx_reservation_status", columnList = "status"),
    @Index(name = "idx_reservation_expiry", columnList = "expiry_date")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "item_variant_id")
    private UUID itemVariantId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "serial_number_id")
    private UUID serialNumberId;

    @Column(name = "qty_reserved", precision = 20, scale = 6, nullable = false)
    private BigDecimal qtyReserved;

    @Column(name = "qty_delivered", precision = 20, scale = 6)
    private BigDecimal qtyDelivered = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "voucher_type", nullable = false, length = 50)
    private String voucherType;

    @Column(name = "voucher_no", nullable = false, length = 100)
    private String voucherNo;

    @Column(name = "voucher_detail_no", length = 100)
    private String voucherDetailNo;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "reference_doctype", length = 50)
    private String referenceDoctype;

    @Column(name = "reference_name", length = 100)
    private String referenceName;

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

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getItemVariantId() { return itemVariantId; }
    public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public UUID getSerialNumberId() { return serialNumberId; }
    public void setSerialNumberId(UUID serialNumberId) { this.serialNumberId = serialNumberId; }

    public BigDecimal getQtyReserved() { return qtyReserved; }
    public void setQtyReserved(BigDecimal qtyReserved) { this.qtyReserved = qtyReserved; }

    public BigDecimal getQtyDelivered() { return qtyDelivered; }
    public void setQtyDelivered(BigDecimal qtyDelivered) { this.qtyDelivered = qtyDelivered; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }

    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }

    public String getVoucherDetailNo() { return voucherDetailNo; }
    public void setVoucherDetailNo(String voucherDetailNo) { this.voucherDetailNo = voucherDetailNo; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getReferenceDoctype() { return referenceDoctype; }
    public void setReferenceDoctype(String referenceDoctype) { this.referenceDoctype = referenceDoctype; }

    public String getReferenceName() { return referenceName; }
    public void setReferenceName(String referenceName) { this.referenceName = referenceName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum ReservationStatus {
        ACTIVE, PARTIALLY_DELIVERED, DELIVERED, CANCELLED, EXPIRED
    }
}