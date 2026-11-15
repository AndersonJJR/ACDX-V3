package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reposting_jobs", indexes = {
    @Index(name = "idx_reposting_status", columnList = "status"),
    @Index(name = "idx_reposting_company_date", columnList = "company_id, posting_date_from, posting_date_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepostingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "job_name", length = 100)
    private String jobName;

    @Column(name = "posting_date_from", nullable = false)
    private LocalDate postingDateFrom;

    @Column(name = "posting_date_to", nullable = false)
    private LocalDate postingDateTo;

    @Column(name = "item_group", length = 50)
    private String itemGroup;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RepostingStatus status = RepostingStatus.PENDING;

    @Column(name = "priority", nullable = false)
    private Integer priority = 5;

    @Column(name = "chunk_size", nullable = false)
    private Integer chunkSize = 1000;

    @Column(name = "total_records")
    private Long totalRecords = 0L;

    @Column(name = "processed_records")
    private Long processedRecords = 0L;

    @Column(name = "failed_records")
    private Long failedRecords = 0L;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_dry_run", nullable = false)
    private Boolean isDryRun = false;

    @Column(name = "triggered_by")
    private UUID triggeredBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum RepostingStatus {
        PENDING, QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED, PARTIALLY_COMPLETED
    }
}