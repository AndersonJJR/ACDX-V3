package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_log", indexes = {
    @Index(name = "idx_time_log_work_order", columnList = "work_order_id"),
    @Index(name = "idx_time_log_employee", columnList = "employee_id"),
    @Index(name = "idx_time_log_date", columnList = "log_date"),
    @Index(name = "idx_time_log_type", columnList = "log_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(name = "work_order_operation_id")
    private UUID workOrderOperationId;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @NotNull
    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false, length = 20)
    private LogType logType;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", precision = 10, scale = 2)
    private BigDecimal durationMinutes = BigDecimal.ZERO;

    @Column(name = "setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal setupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "run_time_minutes", precision = 10, scale = 2)
    private BigDecimal runTimeMinutes = BigDecimal.ZERO;

    @Column(name = "quantity_completed", precision = 19, scale = 6)
    private BigDecimal quantityCompleted = BigDecimal.ZERO;

    @Column(name = "quantity_scrapped", precision = 19, scale = 6)
    private BigDecimal quantityScrapped = BigDecimal.ZERO;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_overtime")
    private Boolean isOvertime = false;

    @Column(name = "overtime_multiplier", precision = 3, scale = 2)
    private BigDecimal overtimeMultiplier = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

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

    public enum LogType {
        SETUP,
        RUN,
        WAIT,
        MOVE,
        REWORK,
        MAINTENANCE,
        MEETING,
        BREAK,
        OTHER
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        AUTO_APPROVED
    }
}