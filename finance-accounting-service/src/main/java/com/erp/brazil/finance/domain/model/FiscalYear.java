package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fiscal_years", uniqueConstraints = {
    @UniqueConstraint(name = "uk_fiscal_year_year", columnNames = "year")
})
public class FiscalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @OneToMany(mappedBy = "fiscalYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FiscalPeriod> periods = new ArrayList<>();

    @OneToMany(mappedBy = "fiscalYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChartOfAccounts> chartOfAccounts = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public FiscalYear() {
        this.createdAt = LocalDate.now();
    }

    public FiscalYear(Integer year, LocalDate startDate, LocalDate endDate) {
        this();
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }
    public LocalDate getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDate closedAt) { this.closedAt = closedAt; }
    public List<FiscalPeriod> getPeriods() { return periods; }
    public void setPeriods(List<FiscalPeriod> periods) { this.periods = periods; }
    public List<ChartOfAccounts> getChartOfAccounts() { return chartOfAccounts; }
    public void setChartOfAccounts(List<ChartOfAccounts> chartOfAccounts) { this.chartOfAccounts = chartOfAccounts; }
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