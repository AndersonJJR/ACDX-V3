package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reconciliations", indexes = {
    @Index(name = "idx_reconciliation_bank_account", columnList = "bank_account_id"),
    @Index(name = "idx_reconciliation_date", columnList = "reconciliation_date"),
    @Index(name = "idx_reconciliation_status", columnList = "status")
})
public class Reconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @NotNull
    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Size(max = 50)
    @Column(name = "reference", length = 50)
    private String reference;

    @NotNull
    @Column(name = "book_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal bookBalance;

    @NotNull
    @Column(name = "bank_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal bankBalance;

    @Column(name = "difference", precision = 19, scale = 2)
    private BigDecimal difference = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReconciliationStatus status = ReconciliationStatus.IN_PROGRESS;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Size(max = 200)
    @Column(name = "completed_by", length = 200)
    private String completedBy;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "reconciliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReconciliationItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Reconciliation() {
        this.createdAt = LocalDateTime.now();
    }

    public Reconciliation(BankAccount bankAccount, LocalDate reconciliationDate,
                          BigDecimal bookBalance, BigDecimal bankBalance) {
        this();
        this.bankAccount = bankAccount;
        this.reconciliationDate = reconciliationDate;
        this.bookBalance = bookBalance;
        this.bankBalance = bankBalance;
        this.difference = bookBalance.subtract(bankBalance);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }
    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public BigDecimal getBookBalance() { return bookBalance; }
    public void setBookBalance(BigDecimal bookBalance) { this.bookBalance = bookBalance; }
    public BigDecimal getBankBalance() { return bankBalance; }
    public void setBankBalance(BigDecimal bankBalance) { this.bankBalance = bankBalance; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public ReconciliationStatus getStatus() { return status; }
    public void setStatus(ReconciliationStatus status) { this.status = status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ReconciliationItem> getItems() { return items; }
    public void setItems(List<ReconciliationItem> items) { this.items = items; }
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