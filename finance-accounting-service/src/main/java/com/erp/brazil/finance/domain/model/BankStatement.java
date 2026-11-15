package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_statements", indexes = {
    @Index(name = "idx_bank_statement_account", columnList = "bank_account_id"),
    @Index(name = "idx_bank_statement_period", columnList = "period_start, period_end"),
    @Index(name = "idx_bank_statement_status", columnList = "status")
})
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @NotNull
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @NotNull
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @NotNull
    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @NotNull
    @Column(name = "final_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal finalBalance;

    @Size(max = 500)
    @Column(name = "file_path", length = 500)
    private String filePath;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BankStatementStatus status = BankStatementStatus.IMPORTED;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Size(max = 200)
    @Column(name = "imported_by", length = 200)
    private String importedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public BankStatement() {
        this.createdAt = LocalDateTime.now();
    }

    public BankStatement(BankAccount bankAccount, LocalDate periodStart, LocalDate periodEnd,
                         BigDecimal initialBalance, BigDecimal finalBalance) {
        this();
        this.bankAccount = bankAccount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    public BigDecimal getFinalBalance() { return finalBalance; }
    public void setFinalBalance(BigDecimal finalBalance) { this.finalBalance = finalBalance; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public BankStatementStatus getStatus() { return status; }
    public void setStatus(BankStatementStatus status) { this.status = status; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
    public String getImportedBy() { return importedBy; }
    public void setImportedBy(String importedBy) { this.importedBy = importedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}