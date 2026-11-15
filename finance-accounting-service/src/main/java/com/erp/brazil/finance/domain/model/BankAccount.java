package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_bank_account_code", columnNames = {"company_id", "code"}),
    @UniqueConstraint(name = "uk_bank_account_pix_key", columnNames = "pix_key")
}, indexes = {
    @Index(name = "idx_bank_account_company", columnList = "company_id")
})
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 10)
    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @NotBlank
    @Size(max = 20)
    @Column(name = "agency", nullable = false, length = 20)
    private String agency;

    @Size(max = 10)
    @Column(name = "agency_digit", length = 10)
    private String agencyDigit;

    @NotBlank
    @Size(max = 30)
    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Size(max = 10)
    @Column(name = "account_digit", length = 10)
    private String accountDigit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private BankAccountType accountType;

    @Size(max = 1000)
    @Column(name = "cnab_config", length = 1000)
    private String cnabConfig;

    @Size(max = 100)
    @Column(name = "pix_key", length = 100)
    private String pixKey;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_of_account_id", nullable = false)
    private ChartOfAccounts chartOfAccount;

    @Column(name = "initial_balance", precision = 19, scale = 2)
    private java.math.BigDecimal initialBalance = java.math.BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private java.math.BigDecimal currentBalance = java.math.BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public BankAccount() {
        this.createdAt = LocalDateTime.now();
    }

    public BankAccount(UUID companyId, String code, String name, String bankCode, String agency,
                       String accountNumber, BankAccountType accountType, ChartOfAccounts chartOfAccount) {
        this();
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.bankCode = bankCode;
        this.agency = agency;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.chartOfAccount = chartOfAccount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getAgency() { return agency; }
    public void setAgency(String agency) { this.agency = agency; }
    public String getAgencyDigit() { return agencyDigit; }
    public void setAgencyDigit(String agencyDigit) { this.agencyDigit = agencyDigit; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountDigit() { return accountDigit; }
    public void setAccountDigit(String accountDigit) { this.accountDigit = accountDigit; }
    public BankAccountType getAccountType() { return accountType; }
    public void setAccountType(BankAccountType accountType) { this.accountType = accountType; }
    public String getCnabConfig() { return cnabConfig; }
    public void setCnabConfig(String cnabConfig) { this.cnabConfig = cnabConfig; }
    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }
    public ChartOfAccounts getChartOfAccount() { return chartOfAccount; }
    public void setChartOfAccount(ChartOfAccounts chartOfAccount) { this.chartOfAccount = chartOfAccount; }
    public java.math.BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(java.math.BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    public java.math.BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(java.math.BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
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