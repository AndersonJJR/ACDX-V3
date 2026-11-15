package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotation_versions")
public class QuotationVersion extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private QuotationStatus status;
    
    @Column(name = "valid_from")
    private LocalDate validFrom;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;
    
    @Column(name = "created_by_id")
    private UUID createdById;
    
    @Column(name = "approved_by_id")
    private UUID approvedById;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;
}