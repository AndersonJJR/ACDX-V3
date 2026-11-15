package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reversal_entries", indexes = {
    @Index(name = "idx_reversal_entry_original", columnList = "original_entry_id"),
    @Index(name = "idx_reversal_entry_reversal_date", columnList = "reversal_date")
})
public class ReversalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_entry_id", nullable = false)
    private JournalEntry originalEntry;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_entry_id", nullable = false)
    private JournalEntry reversalEntry;

    @NotNull
    @Column(name = "reversal_date", nullable = false)
    private LocalDate reversalDate;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @Size(max = 200)
    @Column(name = "reversed_by", length = 200)
    private String reversedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ReversalEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public ReversalEntry(JournalEntry originalEntry, JournalEntry reversalEntry, LocalDate reversalDate, String reason, String reversedBy) {
        this();
        this.originalEntry = originalEntry;
        this.reversalEntry = reversalEntry;
        this.reversalDate = reversalDate;
        this.reason = reason;
        this.reversedBy = reversedBy;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public JournalEntry getOriginalEntry() { return originalEntry; }
    public void setOriginalEntry(JournalEntry originalEntry) { this.originalEntry = originalEntry; }
    public JournalEntry getReversalEntry() { return reversalEntry; }
    public void setReversalEntry(JournalEntry reversalEntry) { this.reversalEntry = reversalEntry; }
    public LocalDate getReversalDate() { return reversalDate; }
    public void setReversalDate(LocalDate reversalDate) { this.reversalDate = reversalDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReversedBy() { return reversedBy; }
    public void setReversedBy(String reversedBy) { this.reversedBy = reversedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}