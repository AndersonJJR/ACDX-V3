package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lead extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "lead_code", length = 20, unique = true, nullable = false)
    private String leadCode;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "source", length = 50)
    private String source;
    
    @Column(name = "score")
    private Integer score = 0;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;
    
    @Column(name = "estimated_value", precision = 15, scale = 2)
    private java.math.BigDecimal estimatedValue;
    
    @Column(name = "probability")
    private Integer probability = 0;
    
    @Column(name = "expected_close_date")
    private LocalDateTime expectedCloseDate;
    
    @Column(name = "qualified_at")
    private LocalDateTime qualifiedAt;
    
    @Column(name = "converted_at")
    private LocalDateTime convertedAt;
    
    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    private Territory territory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sales_person_id")
    private SalesPerson assignedSalesPerson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private SalesPipeline pipeline;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id")
    private PipelineStage currentStage;
    
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Opportunity> opportunities = new ArrayList<>();
    
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LeadActivity> activities = new ArrayList<>();
    
    public boolean isConverted() {
        return status == LeadStatus.CONVERTED;
    }
    
    public boolean isQualified() {
        return status == LeadStatus.QUALIFIED || status == LeadStatus.CONVERTED;
    }
}