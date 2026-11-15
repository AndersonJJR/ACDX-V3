package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_statement_lines", indexes = {
    @Index(name = "idx_bank_statement_line_statement", columnList = "statement_id"),
    @Index(name = "idx_bank_statement_line_date", columnList = "transaction_date"),
    @Index(name = "idx_bank_statement_line_matched", columnList = "matched_entry_id"),
    @Index(name = "idx_bank_statement_line_cnab", columnList = "cnab_code")
})
public class BankStatementLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_id", nullable = false)
    private BankStatement statement;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotNull
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransactionType type;

    @Size(max = 20)
    @Column(name = "cnab_code", length = 20)
    private String cnabCode;

    @Size(max = 100)
    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Size(max = 100)
    @Column(name = "payer_payee", length = 100)
    private String payerPayee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_entry_id")
    private GLEntry matchedEntry;

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

    public BankStatementLine() {
        this.createdAt = LocalDateTime.now();
    }

    public BankStatementLine(BankStatement statement, LocalDate transactionDate, LocalDate valueDate,
                             String description, BigDecimal amount, TransactionType type) {
        this();
        this.statement = statement;
        this.transactionDate = transactionDate;
        this.valueDate = valueDate;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BankStatement getStatement() { return statement; }
    public void setStatement(BankStatement statement) { this.statement = statement; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public LocalDate getValueDate() { return valueDate; }
    public void setValueDate(LocalDate valueDate) { this.valueDate = valueDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getCnabCode() { return cnabCode; }
    public void setCnabCode(String cnabCode) { this.cnabCode = cnabCode; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getPayerPayee() { return payerPayee; }
    public void setPayerPayee(String payerPayee) { this.payerPayee = payerPayee; }
    public GLEntry getMatchedEntry() { return matchedEntry; }
    public void setMatchedEntry(GLEntry matchedEntry) { this.matchedEntry = matchedEntry; }
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