package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_items", indexes = {
    @Index(name = "idx_reconciliation_item_reconciliation", columnList = "reconciliation_id"),
    @Index(name = "idx_reconciliation_item_bank_stmt_line", columnList = "bank_statement_line_id"),
    @Index(name = "idx_reconciliation_item_gl_entry", columnList = "gl_entry_id"),
    @Index(name = "idx_reconciliation_item_status", columnList = "status")
})
public class ReconciliationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private Reconciliation reconciliation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_statement_line_id")
    private BankStatementLine bankStatementLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_entry_id")
    private GLEntry glEntry;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ReconciliationItemType itemType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReconciliationItemStatus status = ReconciliationItemStatus.PENDING;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Size(max = 200)
    @Column(name = "matched_by", length = 200)
    private String matchedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ReconciliationItem() {
        this.createdAt = LocalDateTime.now();
    }

    public ReconciliationItem(Reconciliation reconciliation, ReconciliationItemType itemType, BigDecimal amount) {
        this();
        this.reconciliation = reconciliation;
        this.itemType = itemType;
        this.amount = amount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Reconciliation getReconciliation() { return reconciliation; }
    public void setReconciliation(Reconciliation reconciliation) { this.reconciliation = reconciliation; }
    public BankStatementLine getBankStatementLine() { return bankStatementLine; }
    public void setBankStatementLine(BankStatementLine bankStatementLine) { this.bankStatementLine = bankStatementLine; }
    public GLEntry getGlEntry() { return glEntry; }
    public void setGlEntry(GLEntry glEntry) { this.glEntry = glEntry; }
    public ReconciliationItemType getItemType() { return itemType; }
    public void setItemType(ReconciliationItemType itemType) { this.itemType = itemType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public ReconciliationItemStatus getStatus() { return status; }
    public void setStatus(ReconciliationItemStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    public String getMatchedBy() { return matchedBy; }
    public void setMatchedBy(String matchedBy) { this.matchedBy = matchedBy; }
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