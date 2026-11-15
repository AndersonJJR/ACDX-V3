package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_journal_entry_fiscal_period", columnList = "fiscal_period_id"),
    @Index(name = "idx_journal_entry_entry_date", columnList = "entry_date"),
    @Index(name = "idx_journal_entry_status", columnList = "status"),
    @Index(name = "idx_journal_entry_reference", columnList = "reference")
})
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "reference", nullable = false, length = 50)
    private String reference;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id", nullable = false)
    private FiscalPeriod fiscalPeriod;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JournalEntryStatus status = JournalEntryStatus.DRAFT;

    @NotNull
    @Column(name = "total_debit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_entry_id")
    private JournalEntry reversedEntry;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Size(max = 200)
    @Column(name = "posted_by", length = 200)
    private String postedBy;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntryLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GLEntry> glEntries = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public JournalEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public JournalEntry(String reference, LocalDate entryDate, FiscalPeriod fiscalPeriod, String description) {
        this();
        this.reference = reference;
        this.entryDate = entryDate;
        this.fiscalPeriod = fiscalPeriod;
        this.description = description;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public FiscalPeriod getFiscalPeriod() { return fiscalPeriod; }
    public void setFiscalPeriod(FiscalPeriod fiscalPeriod) { this.fiscalPeriod = fiscalPeriod; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public JournalEntryStatus getStatus() { return status; }
    public void setStatus(JournalEntryStatus status) { this.status = status; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }
    public boolean isReversed() { return reversed; }
    public void setReversed(boolean reversed) { this.reversed = reversed; }
    public JournalEntry getReversedEntry() { return reversedEntry; }
    public void setReversedEntry(JournalEntry reversedEntry) { this.reversedEntry = reversedEntry; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public List<JournalEntryLine> getLines() { return lines; }
    public void setLines(List<JournalEntryLine> lines) { this.lines = lines; }
    public List<GLEntry> getGlEntries() { return glEntries; }
    public void setGlEntries(List<GLEntry> glEntries) { this.glEntries = glEntries; }
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

    public void addLine(JournalEntryLine line) {
        lines.add(line);
        line.setJournalEntry(this);
        recalculateTotals();
    }

    public void removeLine(JournalEntryLine line) {
        lines.remove(line);
        line.setJournalEntry(null);
        recalculateTotals();
    }

    private void recalculateTotals() {
        this.totalDebit = lines.stream()
                .map(JournalEntryLine::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalCredit = lines.stream()
                .map(JournalEntryLine::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return totalDebit.compareTo(totalCredit) == 0;
    }
}