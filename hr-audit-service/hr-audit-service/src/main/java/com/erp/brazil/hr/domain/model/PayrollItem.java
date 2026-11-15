package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_item",
    indexes = {
        @Index(name = "idx_payroll_item_payroll", columnList = "payroll_id"),
        @Index(name = "idx_payroll_item_employee", columnList = "employee_id"),
        @Index(name = "idx_payroll_item_type_category", columnList = "item_type,category")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private PayrollItemType itemType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PayrollItemCategory category;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "reference_value", precision = 12, scale = 2)
    private BigDecimal referenceValue;

    @Column(name = "quantity", precision = 8, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 12, scale = 4)
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "taxable_inss")
    private Boolean taxableInss = false;

    @Column(name = "taxable_irrf")
    private Boolean taxableIrrf = false;

    @Column(name = "taxable_fgts")
    private Boolean taxableFgts = false;

    @Column(name = "is_fixed")
    private Boolean isFixed = false;

    @Size(max = 500)
    @Column(name = "calculation_basis", length = 500)
    private String calculationBasis;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    public boolean isEarning() {
        return itemType == PayrollItemType.EARNING;
    }

    public boolean isDeduction() {
        return itemType == PayrollItemType.DEDUCTION;
    }

    public boolean isEmployerCost() {
        return itemType == PayrollItemType.EMPLOYER_COST;
    }

    public boolean isInformational() {
        return itemType == PayrollItemType.INFORMATIONAL;
    }

    public boolean affectsNetSalary() {
        return itemType.affectsNetSalary();
    }

    public BigDecimal calculateTotal() {
        if (quantity != null && unitPrice != null) {
            return quantity.multiply(unitPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return totalAmount;
    }
}