package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fiscal_periods", uniqueConstraints = {
    @UniqueConstraint(name = "uk_fiscal_period_year_number", columnNames = {"fiscal_year_id", "period_number"})
})
public class FiscalPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", nullable = false)
    private FiscalYear fiscalYear;

    @NotNull
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    @Column(name = "locked_at")
    private LocalDate lockedAt;

    @Size(max = 200)
    @Column(name = "locked_by", length = 200)
    private String lockedBy;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public FiscalPeriod() {
        this.createdAt = LocalDate.now();
    }

    public FiscalPeriod(FiscalYear fiscalYear, Integer periodNumber, LocalDate startDate, LocalDate endDate) {
        this();
        this.fiscalYear = fiscalYear;
        this.periodNumber = periodNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public FiscalYear getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(FiscalYear fiscalYear) { this.fiscalYear = fiscalYear; }
    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public LocalDate getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDate lockedAt) { this.lockedAt = lockedAt; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }
    public LocalDate getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDate closedAt) { this.closedAt = closedAt; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
}