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
@Table(name = "supplier_quotations", indexes = {
    @Index(name = "idx_quotation_supplier", columnList = "supplier_id"),
    @Index(name = "idx_quotation_status", columnList = "status"),
    @Index(name = "idx_quotation_valid_till", columnList = "valid_till")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "quotation_no", nullable = false, length = 50)
    private String quotationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;

    @Column(name = "valid_till")
    private LocalDate validTill;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "exchange_rate", precision = 20, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "total_amount", precision = 20, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount;

    @Column(name = "grand_total", precision = 20, scale = 6)
    private BigDecimal grandTotal;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum QuotationStatus {
        DRAFT, SUBMITTED, ACCEPTED, REJECTED, EXPIRED, ORDERED
    }
}