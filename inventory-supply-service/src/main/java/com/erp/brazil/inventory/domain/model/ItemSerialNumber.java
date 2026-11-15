package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "item_serial_numbers", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "serial_no"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public enum SerialStatus {
        AVAILABLE, RESERVED, DELIVERED, INSTALLED, MAINTENANCE, RETURNED, SCRAPPED
    }

    public enum MaintenanceStatus {
        UNDER_WARRANTY, AMC, OUT_OF_WARRANTY
    }
}