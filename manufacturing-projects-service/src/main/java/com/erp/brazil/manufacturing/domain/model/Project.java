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
@Table(name = "project", indexes = {
    @Index(name = "idx_project_code", columnList = "code", unique = true),
    @Index(name = "idx_project_customer", columnList = "customer_id"),
    @Index(name = "idx_project_status", columnList = "status"),
    @Index(name = "idx_project_manager", columnList = "project_manager_id"),
    @Index(name = "idx_project_dates", columnList = "start_date,end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "project_manager_id")
    private UUID projectManagerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", length = 30)
    private ProjectType projectType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "budget_hours", precision = 10, scale = 2)
    private BigDecimal budgetHours = BigDecimal.ZERO;

    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours = BigDecimal.ZERO;

    @Column(name = "budget_cost", precision = 19, scale = 4)
    private BigDecimal budgetCost = BigDecimal.ZERO;

    @Column(name = "actual_cost", precision = 19, scale = 4)
    private BigDecimal actualCost = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "billing_type", length = 30)
    private String billingType;

    @Column(name = "contract_value", precision = 19, scale = 4)
    private BigDecimal contractValue = BigDecimal.ZERO;

    @Column(name = "invoiced_amount", precision = 19, scale = 4)
    private BigDecimal invoicedAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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

    public enum ProjectStatus {
        PLANNING,
        ACTIVE,
        ON_HOLD,
        COMPLETED,
        CANCELLED,
        CLOSED
    }

    public enum ProjectType {
        FIXED_PRICE,
        TIME_AND_MATERIAL,
        COST_PLUS,
        INTERNAL,
        R_AND_D,
        MAINTENANCE,
        IMPLEMENTATION
    }

    public void addTask(ProjectTask task) {
        tasks.add(task);
        task.setProject(this);
    }

    public void removeTask(ProjectTask task) {
        tasks.remove(task);
        task.setProject(null);
    }

    public void addTimesheet(Timesheet timesheet) {
        timesheets.add(timesheet);
        timesheet.setProject(this);
    }

    public void removeTimesheet(Timesheet timesheet) {
        timesheets.remove(timesheet);
        timesheet.setProject(null);
    }
}