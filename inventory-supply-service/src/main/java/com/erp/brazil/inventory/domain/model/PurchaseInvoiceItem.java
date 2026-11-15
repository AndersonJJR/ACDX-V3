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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
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

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public PurchaseInvoice getPurchaseInvoice() { return purchaseInvoice; }
    public void setPurchaseInvoice(PurchaseInvoice purchaseInvoice) { this.purchaseInvoice = purchaseInvoice; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public PurchaseOrderItem getPurchaseOrderItem() { return purchaseOrderItem; }
    public void setPurchaseOrderItem(PurchaseOrderItem purchaseOrderItem) { this.purchaseOrderItem = purchaseOrderItem; }

    public PurchaseReceiptItem getPurchaseReceiptItem() { return purchaseReceiptItem; }
    public void setPurchaseReceiptItem(PurchaseReceiptItem purchaseReceiptItem) { this.purchaseReceiptItem = purchaseReceiptItem; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public BigDecimal getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(BigDecimal conversionFactor) { this.conversionFactor = conversionFactor; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getTaxTemplate() { return taxTemplate; }
    public void setTaxTemplate(String taxTemplate) { this.taxTemplate = taxTemplate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getPoQty() { return poQty; }
    public void setPoQty(BigDecimal poQty) { this.poQty = poQty; }

    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }

    public BigDecimal getBilledQty() { return billedQty; }
    public void setBilledQty(BigDecimal billedQty) { this.billedQty = billedQty; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public BigDecimal getVarianceQty() { return varianceQty; }
    public void setVarianceQty(BigDecimal varianceQty) { this.varianceQty = varianceQty; }

    public BigDecimal getVarianceAmount() { return varianceAmount; }
    public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum MatchStatus {
        PENDING, MATCHED, QUANTITY_MISMATCH, RATE_MISMATCH, BOTH_MISMATCH, OVER_BILLED
    }
}