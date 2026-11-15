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
    @UniqueConstraint(name = "uk_receivable_invoice_customer_number", columnNames = {"customer_id", "invoice_number", "invoice_series"})
}, indexes = {
    @Index(name = "idx_receivable_invoice_customer", columnList = "customer_id"),
    @Index(name = "idx_receivable_invoice_due_date", columnList = "due_date"),
    @Index(name = "idx_receivable_invoice_status", columnList = "status"),
    @Index(name = "idx_receivable_invoice_fiscal_period", columnList = "fiscal_period_id")
})
public class ReceivableInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

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

    @Column(name = "received_amount", precision = 19, scale = 2)
    private BigDecimal receivedAmount = BigDecimal.ZERO;

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

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 50)
    @Column(name = "sales_order_number", length = 50)
    private String salesOrderNumber;

    @Size(max = 50)
    @Column(name = "delivery_note_number", length = 50)
    private String deliveryNoteNumber;

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

    public ReceivableInvoice(Customer customer, String invoiceNumber, LocalDate invoiceDate, LocalDate dueDate,
                             FiscalPeriod fiscalPeriod, BigDecimal totalAmount, Currency currency,
                             ChartOfAccounts receivableAccount) {
        this();
        this.customer = customer;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.fiscalPeriod = fiscalPeriod;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.receivableAccount = receivableAccount;
        this.outstandingAmount = totalAmount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
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
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSalesOrderNumber() { return salesOrderNumber; }
    public void setSalesOrderNumber(String salesOrderNumber) { this.salesOrderNumber = salesOrderNumber; }
    public String getDeliveryNoteNumber() { return deliveryNoteNumber; }
    public void setDeliveryNoteNumber(String deliveryNoteNumber) { this.deliveryNoteNumber = deliveryNoteNumber; }
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
        this.outstandingAmount = this.totalAmount.subtract(this.receivedAmount);
        if (this.outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InvoiceStatus.PAID;
            this.outstandingAmount = BigDecimal.ZERO;
        } else if (this.receivedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }
}