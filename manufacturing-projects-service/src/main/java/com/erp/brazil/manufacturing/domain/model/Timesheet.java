package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "timesheet", indexes = {
    @Index(name = "idx_timesheet_project", columnList = "project_id"),
    @Index(name = "idx_timesheet_task", columnList = "task_id"),
    @Index(name = "idx_timesheet_employee", columnList = "employee_id"),
    @Index(name = "idx_timesheet_date", columnList = "work_date"),
    @Index(name = "idx_timesheet_status", columnList = "status"),
    @Index(name = "idx_timesheet_period", columnList = "period_start,period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private ProjectTask task;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @NotNull
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "hours_worked", precision = 5, scale = 2)
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_billable")
    private Boolean isBillable = true;

    @Column(name = "billing_rate", precision = 19, scale = 4)
    private BigDecimal billingRate;

    @Column(name = "cost_rate", precision = 19, scale = 4)
    private BigDecimal costRate;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "invoice_id")
    private UUID invoiceId;

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

    public enum TimesheetStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        INVOICED
    }
}