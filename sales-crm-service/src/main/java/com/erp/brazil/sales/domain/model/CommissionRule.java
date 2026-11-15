package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commission_rules")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRule extends BaseEntity {
    
    @Column(name = "rule_code", length = 20, unique = true, nullable = false)
    private String ruleCode;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "rule_type", length = 20, nullable = false)
    private String ruleType; // PERCENTAGE, FIXED_AMOUNT, TIERED, MARGIN_BASED
    
    @Column(name = "base_type", length = 20)
    private String baseType; // REVENUE, MARGIN, QUANTITY, PROFIT
    
    @Column(name = "percentage_rate", precision = 5, scale = 2)
    private BigDecimal percentageRate;
    
    @Column(name = "fixed_amount", precision = 15, scale = 2)
    private BigDecimal fixedAmount;
    
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;
    
    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Column(name = "conditions", columnDefinition = "JSON")
    private String conditions; // JSON with conditions like product_category, customer_type, etc.
    
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
    @Builder.Default
    private List<CommissionTier> tiers = new ArrayList<>();
    
    @OneToMany(mappedBy = "commissionRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommissionPayment> payments = new ArrayList<>();
}