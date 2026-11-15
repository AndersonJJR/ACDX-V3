package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "payment_code", length = 20, unique = true, nullable = false)
    private String paymentCode;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // PIX, BOLETO, CREDIT_CARD, DEBIT_CARD, TRANSFER, CASH
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "bank_agency", length = 10)
    private String bankAgency;
    
    @Column(name = "bank_account", length = 20)
    private String bankAccount;
    
    @Column(name = "pix_key", length = 100)
    private String pixKey;
    
    @Column(name = "boleto_number", length = 50)
    private String boletoNumber;
    
    @Column(name = "boleto_due_date")
    private LocalDate boletoDueDate;
    
    @Column(name = "card_brand", length = 50)
    private String cardBrand;
    
    @Column(name = "card_last_digits", length = 4)
    private String cardLastDigits;
    
    @Column(name = "installments")
    private Integer installments = 1;
    
    @Column(name = "fee_amount", precision = 15, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;
    
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    public Payment() {}
    
    public UUID getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }
    
    public String getPaymentCode() {
        return paymentCode;
    }
    
    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankAgency() {
        return bankAgency;
    }
    
    public void setBankAgency(String bankAgency) {
        this.bankAgency = bankAgency;
    }
    
    public String getBankAccount() {
        return bankAccount;
    }
    
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    public String getPixKey() {
        return pixKey;
    }
    
    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }
    
    public String getBoletoNumber() {
        return boletoNumber;
    }
    
    public void setBoletoNumber(String boletoNumber) {
        this.boletoNumber = boletoNumber;
    }
    
    public LocalDate getBoletoDueDate() {
        return boletoDueDate;
    }
    
    public void setBoletoDueDate(LocalDate boletoDueDate) {
        this.boletoDueDate = boletoDueDate;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public String getCardLastDigits() {
        return cardLastDigits;
    }
    
    public void setCardLastDigits(String cardLastDigits) {
        this.cardLastDigits = cardLastDigits;
    }
    
    public Integer getInstallments() {
        return installments;
    }
    
    public void setInstallments(Integer installments) {
        this.installments = installments;
    }
    
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
    
    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }
    
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public SalesInvoice getSalesInvoice() {
        return salesInvoice;
    }
    
    public void setSalesInvoice(SalesInvoice salesInvoice) {
        this.salesInvoice = salesInvoice;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
}