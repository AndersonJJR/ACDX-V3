package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gl_entries", indexes = {
    @Index(name = "idx_gl_entry_account", columnList = "account_id"),
    @Index(name = "idx_gl_entry_fiscal_period", columnList = "fiscal_period_id"),
    @Index(name = "idx_gl_entry_entry_date", columnList = "entry_date"),
    @Index(name = "idx_gl_entry_journal_entry", columnList = "journal_entry_id")
})
public class GLEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccounts account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_period_id", nullable = false)
    private FiscalPeriod fiscalPeriod;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private java.time.LocalDate entryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "running_balance", precision = 19, scale = 2)
    private BigDecimal runningBalance;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public GLEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public GLEntry(JournalEntry journalEntry, ChartOfAccounts account, FiscalPeriod fiscalPeriod,
                   java.time.LocalDate entryDate, EntryType entryType, BigDecimal amount) {
        this();
        this.journalEntry = journalEntry;
        this.account = account;
        this.fiscalPeriod = fiscalPeriod;
        this.entryDate = entryDate;
        this.entryType = entryType;
        this.amount = amount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
    public ChartOfAccounts getAccount() { return account; }
    public void setAccount(ChartOfAccounts account) { this.account = account; }
    public CostCenter getCostCenter() { return costCenter; }
    public void setCostCenter(CostCenter costCenter) { this.costCenter = costCenter; }
    public FiscalPeriod getFiscalPeriod() { return fiscalPeriod; }
    public void setFiscalPeriod(FiscalPeriod fiscalPeriod) { this.fiscalPeriod = fiscalPeriod; }
    public java.time.LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(java.time.LocalDate entryDate) { this.entryDate = entryDate; }
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRunningBalance() { return runningBalance; }
    public void setRunningBalance(BigDecimal runningBalance) { this.runningBalance = runningBalance; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}