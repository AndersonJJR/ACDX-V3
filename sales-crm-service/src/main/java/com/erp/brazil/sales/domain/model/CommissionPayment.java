package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commission_payments")
public class CommissionPayment extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "payment_code", length = 20, unique = true, nullable = false)
    private String paymentCode;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "gross_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal grossAmount;
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
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

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getPaymentCode() { return paymentCode; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public CommissionStatus getStatus() { return status; }
    public void setStatus(CommissionStatus status) { this.status = status; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public UUID getCalculatedById() { return calculatedById; }
    public void setCalculatedById(UUID calculatedById) { this.calculatedById = calculatedById; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public UUID getApprovedById() { return approvedById; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public UUID getPaidById() { return paidById; }
    public String getPaymentReference() { return paymentReference; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public CommissionRule getCommissionRule() { return commissionRule; }
    public void setCommissionRule(CommissionRule commissionRule) { this.commissionRule = commissionRule; }
    public SalesPerson getSalesPerson() { return salesPerson; }
    public void setSalesPerson(SalesPerson salesPerson) { this.salesPerson = salesPerson; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public SalesInvoice getSalesInvoice() { return salesInvoice; }
    public void setSalesInvoice(SalesInvoice salesInvoice) { this.salesInvoice = salesInvoice; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public void setCommissionRule(CommissionRule commissionRule) { this.commissionRule = commissionRule; }
    public void setSalesPerson(SalesPerson salesPerson) { this.salesPerson = salesPerson; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public void setSalesInvoice(SalesInvoice salesInvoice) { this.salesInvoice = salesInvoice; }
    public void setBaseAmount(BigDecimal baseAmount) { this.grossAmount = baseAmount; }
    public void setCommissionRate(BigDecimal rate) { /* no field for this, using grossAmount */ }
    public void setAmount(BigDecimal amount) { this.grossAmount = amount; }
    public void setStatus(CommissionStatus status) { this.status = status; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public void setCalculatedById(UUID calculatedById) { this.calculatedById = calculatedById; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setPaidById(UUID paidById) { this.paidById = paidById; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public void setNotes(String notes) { this.notes = notes; }
}