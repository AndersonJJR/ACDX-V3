package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "item_serial_numbers", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "serial_no"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class ItemSerialNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ItemBatch batch;

    @Column(name = "serial_no", nullable = false, length = 100)
    private String serialNo;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SerialStatus status = SerialStatus.AVAILABLE;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "purchase_receipt_id")
    private UUID purchaseReceiptId;

    @Column(name = "delivery_note_id")
    private UUID deliveryNoteId;

    @Column(name = "sales_invoice_id")
    private UUID salesInvoiceId;

    @Column(name = "maintenance_status", length = 20)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus maintenanceStatus;

    @Column(name = "warranty_expiry_date")
    private LocalDateTime warrantyExpiryDate;

    @Column(name = "amc_expiry_date")
    private LocalDateTime amcExpiryDate;

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

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public ItemBatch getBatch() { return batch; }
    public void setBatch(ItemBatch batch) { this.batch = batch; }

    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }

    public SerialStatus getStatus() { return status; }
    public void setStatus(SerialStatus status) { this.status = status; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public UUID getPurchaseReceiptId() { return purchaseReceiptId; }
    public void setPurchaseReceiptId(UUID purchaseReceiptId) { this.purchaseReceiptId = purchaseReceiptId; }

    public UUID getDeliveryNoteId() { return deliveryNoteId; }
    public void setDeliveryNoteId(UUID deliveryNoteId) { this.deliveryNoteId = deliveryNoteId; }

    public UUID getSalesInvoiceId() { return salesInvoiceId; }
    public void setSalesInvoiceId(UUID salesInvoiceId) { this.salesInvoiceId = salesInvoiceId; }

    public MaintenanceStatus getMaintenanceStatus() { return maintenanceStatus; }
    public void setMaintenanceStatus(MaintenanceStatus maintenanceStatus) { this.maintenanceStatus = maintenanceStatus; }

    public LocalDateTime getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDateTime warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public LocalDateTime getAmcExpiryDate() { return amcExpiryDate; }
    public void setAmcExpiryDate(LocalDateTime amcExpiryDate) { this.amcExpiryDate = amcExpiryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum SerialStatus {
        AVAILABLE, RESERVED, DELIVERED, INSTALLED, MAINTENANCE, RETURNED, SCRAPPED
    }

    public enum MaintenanceStatus {
        UNDER_WARRANTY, AMC, OUT_OF_WARRANTY
    }
}