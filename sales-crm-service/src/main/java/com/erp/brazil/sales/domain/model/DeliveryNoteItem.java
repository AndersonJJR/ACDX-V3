package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "delivery_note_items")
public class DeliveryNoteItem extends BaseEntity {
    
    @Column(name = "delivery_note_id", nullable = false)
    private UUID deliveryNoteId;
    
    @Column(name = "sales_order_item_id")
    private UUID salesOrderItemId;
    
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(name = "batch_ids", columnDefinition = "TEXT")
    private String batchIds;
    
    @Column(name = "serial_ids", columnDefinition = "TEXT")
    private String serialIds;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", nullable = false, insertable = false, updatable = false)
    private DeliveryNote deliveryNote;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id", insertable = false, updatable = false)
    private SalesOrderItem salesOrderItem;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}