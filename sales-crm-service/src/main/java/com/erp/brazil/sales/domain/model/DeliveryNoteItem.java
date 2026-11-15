package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "delivery_note_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteItem extends BaseEntity {
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_code", length = 50)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "unit", length = 10)
    private String unit = "UN";
    
    @Column(name = "unit_price", precision = 15, scale = 4)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;
    
    @Column(name = "volume", precision = 10, scale = 3)
    private BigDecimal volume;
    
    @Column(name = "batch_number", length = 50)
    private String batchNumber;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", nullable = false)
    private DeliveryNote deliveryNote;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id")
    private SalesOrderItem salesOrderItem;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}