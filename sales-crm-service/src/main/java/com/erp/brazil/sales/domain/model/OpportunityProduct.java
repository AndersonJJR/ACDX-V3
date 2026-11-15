package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "opportunity_products")
public class OpportunityProduct extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_code", length = 50)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "unit_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;
    
    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (quantity != null && unitPrice != null) {
            BigDecimal subtotal = quantity.multiply(unitPrice);
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
            totalPrice = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
    }
}