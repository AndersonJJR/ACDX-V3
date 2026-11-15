package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations", indexes = {
    @Index(name = "idx_payment_alloc_payment", columnList = "payment_id"),
    @Index(name = "idx_payment_alloc_payable_invoice", columnList = "payable_invoice_id"),
    @Index(name = "idx_payment_alloc_receivable_invoice", columnList = "receivable_invoice_id"),
    @Index(name = "idx_payment_alloc_status", columnList = "status")
})
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payable_invoice_id")
    private PayableInvoice payableInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_invoice_id")
    private ReceivableInvoice receivableInvoice;

    @NotNull
    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "interest_amount", precision = 19, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "write_off_amount", precision = 19, scale = 2)
    private BigDecimal writeOffAmount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentAllocationStatus status = PaymentAllocationStatus.PENDING;

    @Column(name = "allocation_date", nullable = false)
    private LocalDateTime allocationDate;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public PaymentAllocation() {
        this.createdAt = LocalDateTime.now();
        this.allocationDate = LocalDateTime.now();
    }

    public PaymentAllocation(Payment payment, BigDecimal allocatedAmount) {
        this();
        this.payment = payment;
        this.allocatedAmount = allocatedAmount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public PayableInvoice getPayableInvoice() { return payableInvoice; }
    public void setPayableInvoice(PayableInvoice payableInvoice) { this.payableInvoice = payableInvoice; }
    public ReceivableInvoice getReceivableInvoice() { return receivableInvoice; }
    public void setReceivableInvoice(ReceivableInvoice receivableInvoice) { this.receivableInvoice = receivableInvoice; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }
    public BigDecimal getWriteOffAmount() { return writeOffAmount; }
    public void setWriteOffAmount(BigDecimal writeOffAmount) { this.writeOffAmount = writeOffAmount; }
    public PaymentAllocationStatus getStatus() { return status; }
    public void setStatus(PaymentAllocationStatus status) { this.status = status; }
    public LocalDateTime getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDateTime allocationDate) { this.allocationDate = allocationDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getNetAmount() {
        return allocatedAmount.subtract(discountAmount).add(interestAmount).subtract(writeOffAmount);
    }
}