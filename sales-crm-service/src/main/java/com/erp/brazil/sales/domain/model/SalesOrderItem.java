package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_order_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem extends BaseEntity {
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_code", length = 50)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "quantity_ordered", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantityOrdered = BigDecimal.ONE;
    
    @Column(name = "quantity_delivered", precision = 15, scale = 3)
    private BigDecimal quantityDelivered = BigDecimal.ZERO;
    
    @Column(name = "quantity_invoiced", precision = 15, scale = 3)
    private BigDecimal quantityInvoiced = BigDecimal.ZERO;
    
    @Column(name = "quantity_pending", precision = 15, scale = 3)
    private BigDecimal quantityPending = BigDecimal.ZERO;
    
    @Column(name = "unit", length = 10)
    private String unit = "UN";
    
    @Column(name = "unit_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @Column(name = "warehouse_id")
    private UUID warehouseId;
    
    @Column(name = "reservation_id")
    private UUID reservationId;
    
    @Column(name = "is_backorder")
    private Boolean isBackorder = false;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;
    
    @OneToMany(mappedBy = "salesOrderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryNoteItem> deliveryNoteItems = new ArrayList<>();
    
    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (quantityOrdered != null && unitPrice != null) {
            BigDecimal subtotal = quantityOrdered.multiply(unitPrice);
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
            BigDecimal afterDiscount = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
            if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                taxAmount = afterDiscount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
            totalPrice = afterDiscount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
            
            // Update pending quantity
            BigDecimal delivered = quantityDelivered != null ? quantityDelivered : BigDecimal.ZERO;
            quantityPending = quantityOrdered.subtract(delivered);
            if (quantityPending.compareTo(BigDecimal.ZERO) < 0) {
                quantityPending = BigDecimal.ZERO;
            }
        }
    }
    
    public BigDecimal getQuantityAvailableForDelivery() {
        return quantityPending;
    }
    
    public BigDecimal getQuantityAvailableForInvoicing() {
        BigDecimal delivered = quantityDelivered != null ? quantityDelivered : BigDecimal.ZERO;
        BigDecimal invoiced = quantityInvoiced != null ? quantityInvoiced : BigDecimal.ZERO;
        return delivered.subtract(invoiced);
    }
    
    public boolean isFullyDelivered() {
        return quantityPending.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isPartiallyDelivered() {
        return quantityDelivered.compareTo(BigDecimal.ZERO) > 0 && !isFullyDelivered();
    }
}