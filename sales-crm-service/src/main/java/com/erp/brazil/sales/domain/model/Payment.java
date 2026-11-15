package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    
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