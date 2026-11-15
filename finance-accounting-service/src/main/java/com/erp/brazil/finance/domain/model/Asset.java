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
@Table(name = "assets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_asset_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_asset_tag", columnNames = "asset_tag")
}, indexes = {
    @Index(name = "idx_asset_category", columnList = "category_id"),
    @Index(name = "idx_asset_location", columnList = "location_id"),
    @Index(name = "idx_asset_status", columnList = "status")
})
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "asset_tag", nullable = false, length = 100)
    private String assetTag;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private AssetLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccounts account;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accumulated_depreciation_account_id", nullable = false)
    private ChartOfAccounts accumulatedDepreciationAccount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depreciation_expense_account_id", nullable = false)
    private ChartOfAccounts depreciationExpenseAccount;

    @NotNull
    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "activation_date")
    private LocalDate activationDate;

    @NotNull
    @Column(name = "acquisition_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(name = "residual_value", precision = 19, scale = 2)
    private BigDecimal residualValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "useful_life_months", nullable = false)
    private Integer usefulLifeMonths;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", nullable = false, length = 20)
    private DepreciationMethod depreciationMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "depreciation_start_date")
    private LocalDate depreciationStartDate;

    @Column(name = "last_depreciation_date")
    private LocalDate lastDepreciationDate;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "disposal_value", precision = 19, scale = 2)
    private BigDecimal disposalValue;

    @Size(max = 500)
    @Column(name = "disposal_reason", length = 500)
    private String disposalReason;

    @Column(name = "accumulated_depreciation", precision = 19, scale = 2)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(name = "book_value", precision = 19, scale = 2)
    private BigDecimal bookValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetDepreciationSchedule> depreciationSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepreciationEntry> depreciationEntries = new ArrayList<>();

    public Asset() {
        this.createdAt = LocalDateTime.now();
    }

    public Asset(String code, String assetTag, String name, AssetCategory category,
                 ChartOfAccounts account, ChartOfAccounts accumulatedDepreciationAccount,
                 ChartOfAccounts depreciationExpenseAccount, LocalDate acquisitionDate,
                 BigDecimal acquisitionCost, Integer usefulLifeMonths, DepreciationMethod depreciationMethod) {
        this();
        this.code = code;
        this.assetTag = assetTag;
        this.name = name;
        this.category = category;
        this.account = account;
        this.accumulatedDepreciationAccount = accumulatedDepreciationAccount;
        this.depreciationExpenseAccount = depreciationExpenseAccount;
        this.acquisitionDate = acquisitionDate;
        this.acquisitionCost = acquisitionCost;
        this.usefulLifeMonths = usefulLifeMonths;
        this.depreciationMethod = depreciationMethod;
        this.bookValue = acquisitionCost;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AssetCategory getCategory() { return category; }
    public void setCategory(AssetCategory category) { this.category = category; }
    public AssetLocation getLocation() { return location; }
    public void setLocation(AssetLocation location) { this.location = location; }
    public CostCenter getCostCenter() { return costCenter; }
    public void setCostCenter(CostCenter costCenter) { this.costCenter = costCenter; }
    public ChartOfAccounts getAccount() { return account; }
    public void setAccount(ChartOfAccounts account) { this.account = account; }
    public ChartOfAccounts getAccumulatedDepreciationAccount() { return accumulatedDepreciationAccount; }
    public void setAccumulatedDepreciationAccount(ChartOfAccounts accumulatedDepreciationAccount) { this.accumulatedDepreciationAccount = accumulatedDepreciationAccount; }
    public ChartOfAccounts getDepreciationExpenseAccount() { return depreciationExpenseAccount; }
    public void setDepreciationExpenseAccount(ChartOfAccounts depreciationExpenseAccount) { this.depreciationExpenseAccount = depreciationExpenseAccount; }
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }
    public LocalDate getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }
    public BigDecimal getAcquisitionCost() { return acquisitionCost; }
    public void setAcquisitionCost(BigDecimal acquisitionCost) { this.acquisitionCost = acquisitionCost; }
    public BigDecimal getResidualValue() { return residualValue; }
    public void setResidualValue(BigDecimal residualValue) { this.residualValue = residualValue; }
    public Integer getUsefulLifeMonths() { return usefulLifeMonths; }
    public void setUsefulLifeMonths(Integer usefulLifeMonths) { this.usefulLifeMonths = usefulLifeMonths; }
    public DepreciationMethod getDepreciationMethod() { return depreciationMethod; }
    public void setDepreciationMethod(DepreciationMethod depreciationMethod) { this.depreciationMethod = depreciationMethod; }
    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }
    public LocalDate getDepreciationStartDate() { return depreciationStartDate; }
    public void setDepreciationStartDate(LocalDate depreciationStartDate) { this.depreciationStartDate = depreciationStartDate; }
    public LocalDate getLastDepreciationDate() { return lastDepreciationDate; }
    public void setLastDepreciationDate(LocalDate lastDepreciationDate) { this.lastDepreciationDate = lastDepreciationDate; }
    public LocalDate getDisposalDate() { return disposalDate; }
    public void setDisposalDate(LocalDate disposalDate) { this.disposalDate = disposalDate; }
    public BigDecimal getDisposalValue() { return disposalValue; }
    public void setDisposalValue(BigDecimal disposalValue) { this.disposalValue = disposalValue; }
    public String getDisposalReason() { return disposalReason; }
    public void setDisposalReason(String disposalReason) { this.disposalReason = disposalReason; }
    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }
    public BigDecimal getBookValue() { return bookValue; }
    public void setBookValue(BigDecimal bookValue) { this.bookValue = bookValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<AssetDepreciationSchedule> getDepreciationSchedules() { return depreciationSchedules; }
    public void setDepreciationSchedules(List<AssetDepreciationSchedule> depreciationSchedules) { this.depreciationSchedules = depreciationSchedules; }
    public List<DepreciationEntry> getDepreciationEntries() { return depreciationEntries; }
    public void setDepreciationEntries(List<DepreciationEntry> depreciationEntries) { this.depreciationEntries = depreciationEntries; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}