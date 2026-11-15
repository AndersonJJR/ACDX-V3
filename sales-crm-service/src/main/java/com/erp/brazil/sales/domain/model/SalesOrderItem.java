package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sales_order_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem extends BaseEntity {
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "unit_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "reserved_qty", precision = 15, scale = 3)
    private BigDecimal reservedQty = BigDecimal.ZERO;
    
    @Column(name = "delivered_qty", precision = 15, scale = 3)
    private BigDecimal deliveredQty = BigDecimal.ZERO;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, insertable = false, updatable = false)
    private SalesOrder salesOrder;
    
    @OneToMany(mappedBy = "salesOrderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryNoteItem> deliveryNoteItems = new ArrayList<>();
    
    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (quantity != null && unitPrice != null) {
            BigDecimal subtotal = quantity.multiply(unitPrice);
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal afterDiscount = subtotal.subtract(discountAmount);
                if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    totalAmount = afterDiscount.add(taxAmount);
                } else {
                    totalAmount = afterDiscount;
                }
            } else {
                if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    totalAmount = subtotal.add(taxAmount);
                } else {
                    totalAmount = subtotal;
                }
            }
        }
    }
    
    public BigDecimal getQuantityAvailableForDelivery() {
        BigDecimal delivered = deliveredQty != null ? deliveredQty : BigDecimal.ZERO;
        return quantity.subtract(delivered);
    }
}