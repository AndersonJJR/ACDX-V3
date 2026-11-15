package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asset_depreciation_schedules", indexes = {
    @Index(name = "idx_depreciation_schedule_asset", columnList = "asset_id"),
    @Index(name = "idx_depreciation_schedule_period", columnList = "period_start, period_end"),
    @Index(name = "idx_depreciation_schedule_posted", columnList = "posted")
})
public class AssetDepreciationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @NotNull
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @NotNull
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @NotNull
    @Column(name = "depreciation_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal depreciationAmount;

    @NotNull
    @Column(name = "accumulated_depreciation", nullable = false, precision = 19, scale = 2)
    private BigDecimal accumulatedDepreciation;

    @NotNull
    @Column(name = "book_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal bookValue;

    @Column(name = "posted", nullable = false)
    private boolean posted = false;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public AssetDepreciationSchedule() {
        this.createdAt = LocalDateTime.now();
    }

    public AssetDepreciationSchedule(Asset asset, LocalDate periodStart, LocalDate periodEnd,
                                     BigDecimal depreciationAmount, BigDecimal accumulatedDepreciation, BigDecimal bookValue) {
        this();
        this.asset = asset;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.depreciationAmount = depreciationAmount;
        this.accumulatedDepreciation = accumulatedDepreciation;
        this.bookValue = bookValue;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getDepreciationAmount() { return depreciationAmount; }
    public void setDepreciationAmount(BigDecimal depreciationAmount) { this.depreciationAmount = depreciationAmount; }
    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }
    public BigDecimal getBookValue() { return bookValue; }
    public void setBookValue(BigDecimal bookValue) { this.bookValue = bookValue; }
    public boolean isPosted() { return posted; }
    public void setPosted(boolean posted) { this.posted = posted; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        // updatedAt handled by version
    }
}