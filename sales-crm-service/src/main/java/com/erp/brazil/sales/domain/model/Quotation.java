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
@Table(name = "quotations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Quotation extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "number", length = 20, unique = true, nullable = false)
    private String number;
    
    @Column(name = "series", length = 10)
    private String series;
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    @Column(name = "parent_quotation_id")
    private UUID parentQuotationId;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;
    
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
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
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
    @JoinColumn(name = "created_by_id")
    private SalesPerson createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private SalesPerson approvedBy;
    
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuotationVersion> versions = new ArrayList<>();
    
    @OneToOne(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private SalesOrder salesOrder;
    
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(validFrom) && !today.isAfter(validUntil);
    }
    
    public boolean isExpired() {
        return LocalDate.now().isAfter(validUntil);
    }
    
    public boolean canBeConverted() {
        return status == QuotationStatus.ACCEPTED && isValid();
    }
}