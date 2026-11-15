package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales_order_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderHistory extends BaseEntity {
    
    @Column(name = "from_status")
    @Enumerated(EnumType.STRING)
    private SalesOrderStatus fromStatus;
    
    @Column(name = "to_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SalesOrderStatus toStatus;
    
    @Column(name = "changed_by_id")
    private UUID changedById;
    
    @Column(name = "changed_by_name", length = 100)
    private String changedByName;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;
}