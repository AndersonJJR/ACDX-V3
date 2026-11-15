package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_invoice_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceItem extends BaseEntity {
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_code", length = 50)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ncm_code", length = 10)
    private String ncmCode;
    
    @Column(name = "cfop_code", length = 4)
    private String cfopCode;
    
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "unit", length = 10)
    private String unit = "UN";
    
    @Column(name = "unit_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_icms_rate", precision = 5, scale = 2)
    private BigDecimal taxIcmsRate = BigDecimal.ZERO;
    
    @Column(name = "tax_icms_amount", precision = 15, scale = 2)
    private BigDecimal taxIcmsAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_ipi_rate", precision = 5, scale = 2)
    private BigDecimal taxIpiRate = BigDecimal.ZERO;
    
    @Column(name = "tax_ipi_amount", precision = 15, scale = 2)
    private BigDecimal taxIpiAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_pis_rate", precision = 5, scale = 2)
    private BigDecimal taxPisRate = BigDecimal.ZERO;
    
    @Column(name = "tax_pis_amount", precision = 15, scale = 2)
    private BigDecimal taxPisAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_cofins_rate", precision = 5, scale = 2)
    private BigDecimal taxCofinsRate = BigDecimal.ZERO;
    
    @Column(name = "tax_cofins_amount", precision = 15, scale = 2)
    private BigDecimal taxCofinsAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_iss_rate", precision = 5, scale = 2)
    private BigDecimal taxIssRate = BigDecimal.ZERO;
    
    @Column(name = "tax_iss_amount", precision = 15, scale = 2)
    private BigDecimal taxIssAmount = BigDecimal.ZERO;
    
    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id")
    private SalesOrderItem salesOrderItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_item_id")
    private DeliveryNoteItem deliveryNoteItem;
}