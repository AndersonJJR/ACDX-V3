package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @UniqueConstraint(name = "uk_payable_invoice_vendor_number_series", columnNames = {"vendor_id", "number", "series"})
}, indexes = {
    @Index(name = "idx_payable_invoice_company", columnList = "company_id"),
    @Index(name = "idx_payable_invoice_vendor", columnList = "vendor_id"),
    @Index(name = "idx_payable_invoice_due_date", columnList = "due_date"),
    @Index(name = "idx_payable_invoice_status", columnList = "status"),
    @Index(name = "idx_payable_invoice_access_key", columnList = "access_key"),
    @Index(name = "idx_payable_invoice_fiscal_period", columnList = "fiscal_period_id")
})
public class PayableInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotBlank
    @Size(max = 50)
    @Column(name = "number", nullable = false, length = 50)
    private String number;

    @Size(max = 10)
    @Column(name = "series", length = 10)
    private String series;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

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

    @Column(name = "balance_amount", precision = 19, scale = 2)
    private BigDecimal balanceAmount;

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

    @Size(max = 500)
    @Column(name = "xml_path", length = 500)
    private String xmlPath;

    @Size(max = 100)
    @Column(name = "access_key", length = 100)
    private String accessKey;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Size(max = 50)
    @Column(name = "purchase_order_number", length = 50)
    private String purchaseOrderNumber;

    @Column(name = "receipt_id")
    private UUID receiptId;

    @Size(max = 50)
    @Column(name = "goods_receipt_number", length = 50)
    private String goodsReceiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id")
    private PaymentTerm paymentTerm;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

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

    public PayableInvoice(UUID companyId, Vendor vendor, String number, String series,
                          LocalDate issueDate, LocalDate dueDate,
                          FiscalPeriod fiscalPeriod, BigDecimal totalAmount,
                          Currency currency, ChartOfAccounts payableAccount) {
        this();
        this.companyId = companyId;
        this.vendor = vendor;
        this.number = number;
        this.series = series;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fiscalPeriod = fiscalPeriod;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.payableAccount = payableAccount;
        this.balanceAmount = totalAmount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
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
    public BigDecimal getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(BigDecimal balanceAmount) { this.balanceAmount = balanceAmount; }
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
    public String getXmlPath() { return xmlPath; }
    public void setXmlPath(String xmlPath) { this.xmlPath = xmlPath; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
    public UUID getReceiptId() { return receiptId; }
    public void setReceiptId(UUID receiptId) { this.receiptId = receiptId; }
    public String getGoodsReceiptNumber() { return goodsReceiptNumber; }
    public void setGoodsReceiptNumber(String goodsReceiptNumber) { this.goodsReceiptNumber = goodsReceiptNumber; }
    public PaymentTerm getPaymentTerm() { return paymentTerm; }
    public void setPaymentTerm(PaymentTerm paymentTerm) { this.paymentTerm = paymentTerm; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
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
        this.balanceAmount = this.totalAmount.subtract(this.paidAmount);
    }

    // Aliases for controller compatibility
    public String getInvoiceNumber() { return getNumber(); }
    public void setInvoiceNumber(String invoiceNumber) { setNumber(invoiceNumber); }
    public String getInvoiceSeries() { return getSeries(); }
    public void setInvoiceSeries(String invoiceSeries) { setSeries(invoiceSeries); }
    public LocalDate getInvoiceDate() { return getIssueDate(); }
    public void setInvoiceDate(LocalDate invoiceDate) { setIssueDate(invoiceDate); }
    public BigDecimal getOutstandingAmount() { return getBalanceAmount(); }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { setBalanceAmount(outstandingAmount); }
}