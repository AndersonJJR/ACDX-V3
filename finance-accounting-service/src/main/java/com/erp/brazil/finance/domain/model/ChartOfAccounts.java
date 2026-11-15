package com.erp.brazil.finance.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chart_of_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_chart_of_accounts_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_chart_of_accounts_name", columnNames = "name")
})
public class ChartOfAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_nature", nullable = false, length = 10)
    private AccountNature accountNature;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_detail", nullable = false)
    private boolean detail = true;

    @Column(name = "level", nullable = false)
    private int level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChartOfAccounts parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChartOfAccounts> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", nullable = false)
    private FiscalYear fiscalYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ChartOfAccounts() {
        this.createdAt = LocalDate.now();
    }

    public ChartOfAccounts(String code, String name, AccountType accountType, AccountNature accountNature, FiscalYear fiscalYear) {
        this();
        this.code = code;
        this.name = name;
        this.accountType = accountType;
        this.accountNature = accountNature;
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public AccountNature getAccountNature() { return accountNature; }
    public void setAccountNature(AccountNature accountNature) { this.accountNature = accountNature; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDetail() { return detail; }
    public void setDetail(boolean detail) { this.detail = detail; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public ChartOfAccounts getParent() { return parent; }
    public void setParent(ChartOfAccounts parent) { this.parent = parent; }
    public List<ChartOfAccounts> getChildren() { return children; }
    public void setChildren(List<ChartOfAccounts> children) { this.children = children; }
    public FiscalYear getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(FiscalYear fiscalYear) { this.fiscalYear = fiscalYear; }
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