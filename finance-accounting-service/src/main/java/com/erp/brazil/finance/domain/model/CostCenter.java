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
@Table(name = "cost_centers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cost_center_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_cost_center_name", columnNames = "name")
})
public class CostCenter {

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
    @Column(name = "cost_center_type", nullable = false, length = 20)
    private CostCenterType costCenterType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CostCenter parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CostCenter> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "budget_amount", precision = 19, scale = 2)
    private java.math.BigDecimal budgetAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public CostCenter() {
        this.createdAt = LocalDate.now();
    }

    public CostCenter(String code, String name, CostCenterType costCenterType) {
        this();
        this.code = code;
        this.name = name;
        this.costCenterType = costCenterType;
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
    public CostCenterType getCostCenterType() { return costCenterType; }
    public void setCostCenterType(CostCenterType costCenterType) { this.costCenterType = costCenterType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public CostCenter getParent() { return parent; }
    public void setParent(CostCenter parent) { this.parent = parent; }
    public List<CostCenter> getChildren() { return children; }
    public void setChildren(List<CostCenter> children) { this.children = children; }
    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
    public java.math.BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(java.math.BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
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