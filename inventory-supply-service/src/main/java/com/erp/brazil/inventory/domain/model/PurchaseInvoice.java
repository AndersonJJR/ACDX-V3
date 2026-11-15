package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_invoices", indexes = {
    @Index(name = "idx_pi_company_no", columnList = "company_id, invoice_no", unique = true),
    @Index(name = "idx_pi_supplier", columnList = "supplier_id"),
    @Index(name = "idx_pi_status", columnList = "status"),
    @Index(name = "idx_pi_date", columnList = "invoice_date"),
    @Index(name = "idx_pi_pr", columnList = "purchase_receipt_id"),
    @Index(name = "idx_pi_po", columnList = "purchase_order_id"),
    @Index(name = "idx_pi_nfe_key", columnList = "nfe_access_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "invoice_no", nullable = false, length = 50)
    private String invoiceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id")
    private PurchaseReceipt purchaseReceipt;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PIStatus status = PIStatus.DRAFT;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "exchange_rate", precision = 20, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "total_qty", precision = 20, scale = 6)
    private BigDecimal totalQty;

    @Column(name = "total_amount", precision = 20, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount;

    @Column(name = "grand_total", precision = 20, scale = 6)
    private BigDecimal grandTotal;

    @Column(name = "advance_allocated", precision = 20, scale = 6)
    private BigDecimal advanceAllocated = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", precision = 20, scale = 6)
    private BigDecimal outstandingAmount;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    // NF-e fields for Brazil
    @Column(name = "nfe_access_key", length = 44)
    private String nfeAccessKey;

    @Column(name = "nfe_number")
    private Long nfeNumber;

    @Column(name = "nfe_series", length = 3)
    private String nfeSeries;

    @Column(name = "nfe_protocol", length = 50)
    private String nfeProtocol;

    @Column(name = "nfe_status", length = 20)
    @Enumerated(EnumType.STRING)
    private NFeStatus nfeStatus;

    @Column(name = "nfe_xml", columnDefinition = "LONGTEXT")
    private String nfeXml;

    @Column(name = "cfop", length = 4)
    private String cfop;

    @Column(name = "natureza_operacao", length = 60)
    private String naturezaOperacao;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_blocked_for_payment", nullable = false)
    private Boolean isBlockedForPayment = false;

    @Column(name = "block_reason", columnDefinition = "TEXT")
    private String blockReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum PIStatus {
        DRAFT, SUBMITTED, VALIDATED, APPROVED, BLOCKED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED, RETURNED
    }

    public enum NFeStatus {
        PENDING, AUTHORIZED, DENIED, CANCELLED, INUTILIZED
    }
}