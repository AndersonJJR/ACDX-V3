package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "commission_tiers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionTier extends BaseEntity {
    
    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder;
    
    @Column(name = "min_value", precision = 15, scale = 2)
    private BigDecimal minValue;
    
    @Column(name = "max_value", precision = 15, scale = 2)
    private BigDecimal maxValue;
    
    @Column(name = "percentage_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentageRate;
    
    @Column(name = "fixed_amount", precision = 15, scale = 2)
    private BigDecimal fixedAmount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_rule_id", nullable = false)
    private CommissionRule commissionRule;
}