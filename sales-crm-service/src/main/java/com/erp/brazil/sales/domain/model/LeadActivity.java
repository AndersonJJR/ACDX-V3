package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lead_activities")
public class LeadActivity extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType; // CALL, EMAIL, MEETING, NOTE, TASK
    
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
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private SalesPerson createdBy;
}