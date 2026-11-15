package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "corrective_action", indexes = {
    @Index(name = "idx_ca_nc", columnList = "non_conformance_id"),
    @Index(name = "idx_ca_status", columnList = "status"),
    @Index(name = "idx_ca_assignee", columnList = "assigned_to"),
    @Index(name = "idx_ca_dates", columnList = "planned_completion_date,actual_completion_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectiveAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_conformance_id", nullable = false)
    private NonConformance nonConformance;

    @NotNull
    @Size(max = 100)
    @Column(name = "action_number", nullable = false, length = 100)
    private String actionNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ActionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ActionStatus status = ActionStatus.PLANNED;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "root_cause_analysis", length = 2000)
    private String rootCauseAnalysis;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_completion_date")
    private LocalDateTime plannedCompletionDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Column(name = "verification_method", length = 1000)
    private String verificationMethod;

    @Column(name = "verification_result", length = 1000)
    private String verificationResult;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "effectiveness_check_date")
    private LocalDateTime effectivenessCheckDate;

    @Column(name = "effectiveness_result", length = 1000)
    private String effectivenessResult;

    @Column(name = "cost", precision = 19, scale = 4)
    private java.math.BigDecimal cost = java.math.BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Version
    private Long versionLock;

    public enum ActionType {
        CORRECTIVE,
        PREVENTIVE,
        IMPROVEMENT
    }

    public enum ActionStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        VERIFIED,
        EFFECTIVE,
        CANCELLED,
        DEFERRED
    }
}