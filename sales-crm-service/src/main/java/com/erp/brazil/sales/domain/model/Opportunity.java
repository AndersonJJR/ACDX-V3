package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Opportunity extends BaseEntity {
    
    @Column(name = "opportunity_code", length = 20, unique = true, nullable = false)
    private String opportunityCode;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OpportunityStatus status = OpportunityStatus.OPEN;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "probability")
    private Integer probability = 10;
    
    @Column(name = "weighted_amount", precision = 15, scale = 2)
    private BigDecimal weightedAmount;
    
    @Column(name = "expected_close_date")
    private LocalDateTime expectedCloseDate;
    
    @Column(name = "actual_close_date")
    private LocalDateTime actualCloseDate;
    
    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;
    
    @Column(name = "lost_to_competitor", length = 100)
    private String lostToCompetitor;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    private Territory territory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private SalesPerson assignedTo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private SalesPipeline pipeline;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private PipelineStage stage;
    
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quotation> quotations = new ArrayList<>();
    
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpportunityActivity> activities = new ArrayList<>();
    
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpportunityProduct> products = new ArrayList<>();
    
    @PrePersist
    @PreUpdate
    public void calculateWeightedAmount() {
        if (amount != null && probability != null) {
            weightedAmount = amount.multiply(BigDecimal.valueOf(probability)).divide(BigDecimal.valueOf(100));
        }
    }
    
    public boolean isWon() {
        return status == OpportunityStatus.WON;
    }
    
    public boolean isLost() {
        return status == OpportunityStatus.LOST || status == OpportunityStatus.CANCELLED;
    }
    
    public boolean isOpen() {
        return status == OpportunityStatus.OPEN || status == OpportunityStatus.IN_PROGRESS 
            || status == OpportunityStatus.QUOTATION_SENT || status == OpportunityStatus.NEGOTIATION;
    }
}