package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_item",
    uniqueConstraints = @UniqueConstraint(name = "uk_payroll_item_uuid", columnNames = "uuid"),
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
    private ItemType itemType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

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
    @DecimalMin("0.00")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "taxable_inss")
    private boolean taxableInss = false;

    @Column(name = "taxable_irrf")
    private boolean taxableIrrf = false;

    @Column(name = "taxable_fgts")
    private boolean taxableFgts = false;

    @Column(name = "is_fixed")
    private boolean isFixed = false;

    @Column(name = "calculation_basis", columnDefinition = "TEXT")
    private String calculationBasis;

    @Column(name = "display_order")
    private int displayOrder = 0;

    public enum ItemType {
        EARNING, DEDUCTION, EMPLOYER_COST, INFORMATIONAL
    }

    public enum Category {
        SALARY_BASE, OVERTIME, NIGHT_SHIFT, HAZARDOUS, UNHEALTHY,
        VACATION, THIRTEENTH, NOTICE_PERIOD, FGTS, INSS_EMPLOYEE, INSS_EMPLOYER,
        IRRF, TRANSPORT_VOUCHER, MEAL_VOUCHER, HEALTH_INSURANCE, DENTAL_INSURANCE,
        LIFE_INSURANCE, UNION_DUES, ADVANCE, LOAN, GARNISHMENT, TAX, OTHER
    }
}