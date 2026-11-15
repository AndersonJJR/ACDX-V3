package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payable_invoices", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payable_invoice_vendor_number", columnNames = {"vendor_id", "invoice_number", "invoice_series"})
}, indexes = {
    @Index(name = "idx_payable_invoice_vendor", columnList = "vendor_id"),
    @Index(name = "idx_payable_invoice_due_date", columnList = "due_date"),
    @Index(name = "idx_payable_invoice_status", columnList = "status"),
    @Index(name = "idx_payable_invoice_fiscal_period", columnList = "fiscal_period_id")
})
public class PayableInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotBlank
    @Size(max = 50)
    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Size(max = 10)
    @Column(name = "invoice_series", length = 10)
    private String invoiceSeries;

    @NotNull
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id", nullable = false)
    private FiscalPeriod fiscalPeriod;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", precision = 19, scale = 2)
    private BigDecimal outstandingAmount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payable_account_id", nullable = false)
    private ChartOfAccounts payableAccount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Size(max = 200)
    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 50)
    @Column(name = "purchase_order_number", length = 50)
    private String purchaseOrderNumber;

    @Size(max = 50)
    @Column(name = "goods_receipt_number", length = 50)
    private String goodsReceiptNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "payableInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAllocation> paymentAllocations = new ArrayList<>();

    public PayableInvoice() {
        this.createdAt = LocalDateTime.now();
    }

    public PayableInvoice(Vendor vendor, String invoiceNumber, LocalDate invoiceDate, LocalDate dueDate,
                          FiscalPeriod fiscalPeriod, BigDecimal totalAmount, Currency currency,
                          ChartOfAccounts payableAccount) {
        this();
        this.vendor = vendor;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.fiscalPeriod = fiscalPeriod;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.payableAccount = payableAccount;
        this.outstandingAmount = totalAmount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getInvoiceSeries() { return invoiceSeries; }
    public void setInvoiceSeries(String invoiceSeries) { this.invoiceSeries = invoiceSeries; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public FiscalPeriod getFiscalPeriod() { return fiscalPeriod; }
    public void setFiscalPeriod(FiscalPeriod fiscalPeriod) { this.fiscalPeriod = fiscalPeriod; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public ChartOfAccounts getPayableAccount() { return payableAccount; }
    public void setPayableAccount(ChartOfAccounts payableAccount) { this.payableAccount = payableAccount; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
    public String getGoodsReceiptNumber() { return goodsReceiptNumber; }
    public void setGoodsReceiptNumber(String goodsReceiptNumber) { this.goodsReceiptNumber = goodsReceiptNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<PaymentAllocation> getPaymentAllocations() { return paymentAllocations; }
    public void setPaymentAllocations(List<PaymentAllocation> paymentAllocations) { this.paymentAllocations = paymentAllocations; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void recalculateOutstanding() {
        this.outstandingAmount = this.totalAmount.subtract(this.paidAmount);
        if (this.outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InvoiceStatus.PAID;
            this.outstandingAmount = BigDecimal.ZERO;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }
}