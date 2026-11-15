package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commission_rules")
public class CommissionRule extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "rule_code", length = 20, unique = true, nullable = false)
    private String ruleCode;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "rule_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType type = RuleType.PERCENTAGE;
    
    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;
    
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;
    
    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "product_category_id")
    private UUID productCategoryId;
    
    @Column(name = "customer_segment", length = 50)
    private String customerSegment;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_team_id")
    private SalesTeam salesTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private SalesPerson salesPerson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    private Territory territory;
    
    @OneToMany(mappedBy = "commissionRule", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionTier> tiers = new ArrayList<>();
    
    @OneToMany(mappedBy = "commissionRule", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionPayment> payments = new ArrayList<>();
    
    public enum RuleType {
        PERCENTAGE,
        FIXED,
        TIERED
    }

    public UUID getCompanyId() { return companyId; }
    public String getRuleCode() { return ruleCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RuleType getType() { return type; }
    public BigDecimal getValue() { return value; }
    public BigDecimal getMinAmount() { return minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public UUID getProductCategoryId() { return productCategoryId; }
    public String getCustomerSegment() { return customerSegment; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidUntil() { return validUntil; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getPriority() { return priority; }
    public SalesTeam getSalesTeam() { return salesTeam; }
    public void setSalesTeam(SalesTeam salesTeam) { this.salesTeam = salesTeam; }
    public SalesPerson getSalesPerson() { return salesPerson; }
    public void setSalesPerson(SalesPerson salesPerson) { this.salesPerson = salesPerson; }
    public Territory getTerritory() { return territory; }
    public void setTerritory(Territory territory) { this.territory = territory; }
    public List<CommissionTier> getTiers() { return tiers; }
    public List<CommissionPayment> getPayments() { return payments; }
}