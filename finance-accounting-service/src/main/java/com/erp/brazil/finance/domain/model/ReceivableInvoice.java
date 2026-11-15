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
@Table(name = "receivable_invoices", uniqueConstraints = {
    @UniqueConstraint(name = "uk_receivable_invoice_customer_number_series", columnNames = {"customer_id", "number", "series"})
}, indexes = {
    @Index(name = "idx_receivable_invoice_company", columnList = "company_id"),
    @Index(name = "idx_receivable_invoice_customer", columnList = "customer_id"),
    @Index(name = "idx_receivable_invoice_due_date", columnList = "due_date"),
    @Index(name = "idx_receivable_invoice_status", columnList = "status"),
    @Index(name = "idx_receivable_invoice_access_key", columnList = "access_key"),
    @Index(name = "idx_receivable_invoice_fiscal_period", columnList = "fiscal_period_id")
})
public class ReceivableInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

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

    @Column(name = "received_amount", precision = 19, scale = 2)
    private BigDecimal receivedAmount = BigDecimal.ZERO;

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
    @JoinColumn(name = "receivable_account_id", nullable = false)
    private ChartOfAccounts receivableAccount;

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

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Size(max = 50)
    @Column(name = "sales_order_number", length = 50)
    private String salesOrderNumber;

    @Column(name = "delivery_note_id")
    private UUID deliveryNoteId;

    @Size(max = 50)
    @Column(name = "delivery_note_number", length = 50)
    private String deliveryNoteNumber;

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

    @OneToMany(mappedBy = "receivableInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAllocation> paymentAllocations = new ArrayList<>();

    public ReceivableInvoice() {
        this.createdAt = LocalDateTime.now();
    }

    public ReceivableInvoice(UUID companyId, Customer customer, String number, String series,
                             LocalDate issueDate, LocalDate dueDate,
                             FiscalPeriod fiscalPeriod, BigDecimal totalAmount,
                             Currency currency, ChartOfAccounts receivableAccount) {
        this();
        this.companyId = companyId;
        this.customer = customer;
        this.number = number;
        this.series = series;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fiscalPeriod = fiscalPeriod;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.receivableAccount = receivableAccount;
        this.balanceAmount = totalAmount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
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
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public BigDecimal getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(BigDecimal balanceAmount) { this.balanceAmount = balanceAmount; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public ChartOfAccounts getReceivableAccount() { return receivableAccount; }
    public void setReceivableAccount(ChartOfAccounts receivableAccount) { this.receivableAccount = receivableAccount; }
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
    public UUID getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(UUID salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getSalesOrderNumber() { return salesOrderNumber; }
    public void setSalesOrderNumber(String salesOrderNumber) { this.salesOrderNumber = salesOrderNumber; }
    public UUID getDeliveryNoteId() { return deliveryNoteId; }
    public void setDeliveryNoteId(UUID deliveryNoteId) { this.deliveryNoteId = deliveryNoteId; }
    public String getDeliveryNoteNumber() { return deliveryNoteNumber; }
    public void setDeliveryNoteNumber(String deliveryNoteNumber) { this.deliveryNoteNumber = deliveryNoteNumber; }
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
        this.balanceAmount = this.totalAmount.subtract(this.receivedAmount);
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