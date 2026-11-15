package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "number", length = 20, unique = true, nullable = false)
    private String number;
    
    @Column(name = "series", length = 10)
    private String series;
    
    @Column(name = "quotation_id")
    private UUID quotationId;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;
    
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "confirmed_by_id")
    private UUID confirmedById;
    
    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_amount", precision = 15, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    private String currency = "BRL";
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;
    
    @Column(name = "delivery_terms", length = 100)
    private String deliveryTerms;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by_id")
    private UUID cancelledById;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", insertable = false, updatable = false)
    private Quotation quotation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private SalesPerson salesPerson;
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryNote> deliveryNotes = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesInvoice> invoices = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderHistory> history = new ArrayList<>();
    
    public boolean canBeConfirmed() {
        return status == SalesOrderStatus.DRAFT && !items.isEmpty();
    }
    
    public boolean canBeCancelled() {
        return status == SalesOrderStatus.DRAFT || status == SalesOrderStatus.CONFIRMED 
            || status == SalesOrderStatus.ON_HOLD;
    }
    
    public boolean isFullyDelivered() {
        return status == SalesOrderStatus.DELIVERED || status == SalesOrderStatus.INVOICED;
    }
    
    public boolean isPartiallyDelivered() {
        return status == SalesOrderStatus.PARTIALLY_DELIVERED;
    }
}