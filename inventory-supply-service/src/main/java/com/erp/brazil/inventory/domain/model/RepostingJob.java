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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
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

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public LocalDate getPostingDateFrom() { return postingDateFrom; }
    public void setPostingDateFrom(LocalDate postingDateFrom) { this.postingDateFrom = postingDateFrom; }

    public LocalDate getPostingDateTo() { return postingDateTo; }
    public void setPostingDateTo(LocalDate postingDateTo) { this.postingDateTo = postingDateTo; }

    public String getItemGroup() { return itemGroup; }
    public void setItemGroup(String itemGroup) { this.itemGroup = itemGroup; }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public RepostingStatus getStatus() { return status; }
    public void setStatus(RepostingStatus status) { this.status = status; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public Long getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(Long processedRecords) { this.processedRecords = processedRecords; }

    public Long getFailedRecords() { return failedRecords; }
    public void setFailedRecords(Long failedRecords) { this.failedRecords = failedRecords; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Boolean getIsDryRun() { return isDryRun; }
    public void setIsDryRun(Boolean isDryRun) { this.isDryRun = isDryRun; }

    public UUID getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(UUID triggeredBy) { this.triggeredBy = triggeredBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum RepostingStatus {
        PENDING, QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED, PARTIALLY_COMPLETED
    }
}