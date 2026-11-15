package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items", indexes = {
    @Index(name = "idx_po_item_po", columnList = "purchase_order_id"),
    @Index(name = "idx_po_item_item", columnList = "item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @Column(name = "qty", precision = 20, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "received_qty", precision = 20, scale = 6)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(name = "billed_qty", precision = 20, scale = 6)
    private BigDecimal billedQty = BigDecimal.ZERO;

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

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "required_date")
    private LocalDateTime requiredDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_subcontracted", nullable = false)
    private Boolean isSubcontracted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}