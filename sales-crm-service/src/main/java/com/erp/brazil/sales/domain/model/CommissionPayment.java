package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commission_payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPayment extends BaseEntity {
    
    @Column(name = "payment_code", length = 20, unique = true, nullable = false)
    private String paymentCode;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "base_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseAmount;
    
    @Column(name = "commission_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionAmount;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CommissionStatus status = CommissionStatus.PENDING;
    
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
    
    @Column(name = "calculated_by_id")
    private UUID calculatedById;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by_id")
    private UUID approvedById;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "paid_by_id")
    private UUID paidById;
    
    @Column(name = "payment_reference", length = 50)
    private String paymentReference;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_rule_id", nullable = false)
    private CommissionRule commissionRule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id", nullable = false)
    private SalesPerson salesPerson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id")
    private SalesInvoice salesInvoice;
}