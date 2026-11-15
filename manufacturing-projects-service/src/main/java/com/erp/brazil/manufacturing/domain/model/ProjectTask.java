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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project_task", indexes = {
    @Index(name = "idx_task_project", columnList = "project_id"),
    @Index(name = "idx_task_parent", columnList = "parent_task_id"),
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_assignee", columnList = "assignee_id"),
    @Index(name = "idx_task_dates", columnList = "start_date,end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "parent_task_id")
    private UUID parentTaskId;

    @NotNull
    @Size(max = 100)
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.NORMAL;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours = BigDecimal.ZERO;

    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours = BigDecimal.ZERO;

    @Column(name = "remaining_hours", precision = 10, scale = 2)
    private BigDecimal remainingHours = BigDecimal.ZERO;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Column(name = "is_milestone")
    private Boolean isMilestone = false;

    @Column(name = "is_billable")
    private Boolean isBillable = true;

    @Column(name = "billing_rate", precision = 19, scale = 4)
    private BigDecimal billingRate;

    @Column(name = "cost_rate", precision = 19, scale = 4)
    private BigDecimal costRate;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectTask> subTasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Timesheet> timesheets = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long versionLock;

    public enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        CANCELLED,
        DEFERRED
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT,
        CRITICAL
    }

    public void addSubTask(ProjectTask subTask) {
        subTasks.add(subTask);
        subTask.setParentTaskId(this.id);
    }

    public void removeSubTask(ProjectTask subTask) {
        subTasks.remove(subTask);
        subTask.setParentTaskId(null);
    }

    public void addTimesheet(Timesheet timesheet) {
        timesheets.add(timesheet);
        timesheet.setTask(this);
    }

    public void removeTimesheet(Timesheet timesheet) {
        timesheets.remove(timesheet);
        timesheet.setTask(null);
    }
}