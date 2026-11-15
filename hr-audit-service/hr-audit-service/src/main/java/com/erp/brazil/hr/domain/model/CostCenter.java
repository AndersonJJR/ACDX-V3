package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cost_center",
    uniqueConstraints = @UniqueConstraint(name = "uk_cost_center_company_code", columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenter extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CostCenter parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CostCenter> children = new ArrayList<>();

    @Column(name = "responsible_id")
    private Long responsibleId;

    @Column(name = "budget_amount", precision = 15, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public boolean isRoot() {
        return parent == null;
    }
}