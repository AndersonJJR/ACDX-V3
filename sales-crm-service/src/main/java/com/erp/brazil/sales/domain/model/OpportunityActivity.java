package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "opportunity_activities")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityActivity extends BaseEntity {
    
    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType; // CALL, EMAIL, MEETING, DEMO, PROPOSAL, NOTE, TASK
    
    @Column(name = "subject", length = 200)
    private String subject;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "outcome", length = 50)
    private String outcome;
    
    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;
    
    @Column(name = "next_action_date")
    private LocalDateTime nextActionDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private SalesPerson createdBy;
}