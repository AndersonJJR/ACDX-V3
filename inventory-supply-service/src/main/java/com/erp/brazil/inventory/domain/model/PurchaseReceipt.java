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
@Table(name = "purchase_receipts", indexes = {
    @Index(name = "idx_pr_company_no", columnList = "company_id, receipt_no", unique = true),
    @Index(name = "idx_pr_supplier", columnList = "supplier_id"),
    @Index(name = "idx_pr_status", columnList = "status"),
    @Index(name = "idx_pr_date", columnList = "receipt_date"),
    @Index(name = "idx_pr_po", columnList = "purchase_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PRStatus status = PRStatus.DRAFT;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "exchange_rate", precision = 20, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "total_qty", precision = 20, scale = 6)
    private BigDecimal totalQty;

    @Column(name = "total_amount", precision = 20, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount;

    @Column(name = "grand_total", precision = 20, scale = 6)
    private BigDecimal grandTotal;

    @Column(name = "inspection_required", nullable = false)
    private Boolean inspectionRequired = false;

    @Column(name = "inspection_by")
    private UUID inspectionBy;

    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;

    @Column(name = "transporter_name", length = 100)
    private String transporterName;

    @Column(name = "lr_no", length = 50)
    private String lrNo;

    @Column(name = "lr_date")
    private LocalDate lrDate;

    @Column(name = "vehicle_no", length = 20)
    private String vehicleNo;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "per_billed", precision = 10, scale = 2)
    private BigDecimal perBilled = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum PRStatus {
        DRAFT, SUBMITTED, QUALITY_INSPECTION, ACCEPTED, REJECTED, PARTIALLY_RECEIVED, COMPLETED, RETURNED, CANCELLED
    }
}