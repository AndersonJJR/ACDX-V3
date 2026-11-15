package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_invoice_items", indexes = {
    @Index(name = "idx_pii_invoice", columnList = "purchase_invoice_id"),
    @Index(name = "idx_pii_item", columnList = "item_id"),
    @Index(name = "idx_pii_pr_item", columnList = "purchase_receipt_item_id"),
    @Index(name = "idx_pii_po_item", columnList = "purchase_order_item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_item_id")
    private PurchaseReceiptItem purchaseReceiptItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @Column(name = "qty", precision = 20, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "conversion_factor", precision = 20, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    @Column(name = "rate", precision = 20, scale = 6, nullable = false)
    private BigDecimal rate;

    @Column(name = "amount", precision = 20, scale = 6)
    private BigDecimal amount;

    @Column(name = "discount_percent", precision = 10, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 20, scale = 6)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_template", length = 50)
    private String taxTemplate;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // 3-way match fields
    @Column(name = "po_qty", precision = 20, scale = 6)
    private BigDecimal poQty;

    @Column(name = "received_qty", precision = 20, scale = 6)
    private BigDecimal receivedQty;

    @Column(name = "billed_qty", precision = 20, scale = 6)
    private BigDecimal billedQty;

    @Column(name = "match_status", length = 20)
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus = MatchStatus.PENDING;

    @Column(name = "variance_qty", precision = 20, scale = 6)
    private BigDecimal varianceQty;

    @Column(name = "variance_amount", precision = 20, scale = 6)
    private BigDecimal varianceAmount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum MatchStatus {
        PENDING, MATCHED, QUANTITY_MISMATCH, RATE_MISMATCH, BOTH_MISMATCH, OVER_BILLED
    }
}