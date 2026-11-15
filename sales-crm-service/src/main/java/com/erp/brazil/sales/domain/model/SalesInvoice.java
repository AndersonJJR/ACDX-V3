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
@Table(name = "sales_invoices")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoice extends BaseEntity {
    
    @Column(name = "invoice_code", length = 20, unique = true, nullable = false)
    private String invoiceCode;
    
    @Column(name = "nf_number", length = 20)
    private String nfNumber;
    
    @Column(name = "nf_series", length = 10)
    private String nfSeries;
    
    @Column(name = "nf_key", length = 50)
    private String nfKey;
    
    @Column(name = "nf_protocol", length = 50)
    private String nfProtocol;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "operation_date")
    private LocalDate operationDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "invoice_type", length = 20)
    private String invoiceType; // NFe, NFCe, CFe
    
    @Column(name = "nature_of_operation", length = 60)
    private String natureOfOperation;
    
    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_icms", precision = 15, scale = 2)
    private BigDecimal taxIcms = BigDecimal.ZERO;
    
    @Column(name = "tax_ipi", precision = 15, scale = 2)
    private BigDecimal taxIpi = BigDecimal.ZERO;
    
    @Column(name = "tax_pis", precision = 15, scale = 2)
    private BigDecimal taxPis = BigDecimal.ZERO;
    
    @Column(name = "tax_cofins", precision = 15, scale = 2)
    private BigDecimal taxCofins = BigDecimal.ZERO;
    
    @Column(name = "tax_iss", precision = 15, scale = 2)
    private BigDecimal taxIss = BigDecimal.ZERO;
    
    @Column(name = "total_tax", precision = 15, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;
    
    @Column(name = "freight_value", precision = 15, scale = 2)
    private BigDecimal freightValue = BigDecimal.ZERO;
    
    @Column(name = "insurance_value", precision = 15, scale = 2)
    private BigDecimal insuranceValue = BigDecimal.ZERO;
    
    @Column(name = "other_expenses", precision = 15, scale = 2)
    private BigDecimal otherExpenses = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    private String currency = "BRL";
    
    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by_id")
    private UUID cancelledById;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;
    
    @Column(name = "pdf_content", columnDefinition = "LONGBLOB")
    private byte[] pdfContent;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;
    
    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesInvoiceItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
    
    public BigDecimal getOutstandingAmount() {
        BigDecimal paid = payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalAmount.subtract(paid);
    }
    
    public boolean isFullyPaid() {
        return getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && !isFullyPaid();
    }
}