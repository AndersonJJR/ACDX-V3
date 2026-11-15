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
@Table(name = "subcontracting_receipt", indexes = {
    @Index(name = "idx_sub_receipt_order", columnList = "subcontracting_order_id"),
    @Index(name = "idx_sub_receipt_number", columnList = "receipt_number", unique = true),
    @Index(name = "idx_sub_receipt_date", columnList = "receipt_date"),
    @Index(name = "idx_sub_receipt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcontractingReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontracting_order_id", nullable = false)
    private SubcontractingOrder subcontractingOrder;

    @NotNull
    @Size(max = 50)
    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @NotNull
    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "received_quantity", precision = 19, scale = 6)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "accepted_quantity", precision = 19, scale = 6)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", precision = 19, scale = 6)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "quality_inspection_id")
    private UUID qualityInspectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReceiptStatus status = ReceiptStatus.PENDING_INSPECTION;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "vendor_invoice_number", length = 100)
    private String vendorInvoiceNumber;

    @Column(name = "vendor_invoice_date")
    private LocalDateTime vendorInvoiceDate;

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

    public enum ReceiptStatus {
        PENDING_INSPECTION,
        INSPECTION_PASSED,
        INSPECTION_FAILED,
        PARTIAL_ACCEPTED,
        ACCEPTED,
        REJECTED,
        RETURNED_TO_VENDOR
    }
}