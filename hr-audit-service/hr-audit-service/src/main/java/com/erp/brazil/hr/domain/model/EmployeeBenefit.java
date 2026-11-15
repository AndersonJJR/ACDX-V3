package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_benefit",
    indexes = @Index(name = "idx_employee_benefit_employee", columnList = "employee_id"),
    uniqueConstraints = @UniqueConstraint(name = "uk_employee_benefit_uuid", columnNames = "uuid"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeBenefit extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_id")
    private Dependent dependent;

    @NotNull
    @Column(name = "employee_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal employeeValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "employer_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal employerValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EmployeeBenefitStatus status = EmployeeBenefitStatus.ACTIVE;

    @Size(max = 500)
    @Column(name = "observations", length = 500)
    private String observations;

    public boolean isActiveOn(LocalDate date) {
        return status == EmployeeBenefitStatus.ACTIVE &&
               !startDate.isAfter(date) &&
               (endDate == null || !endDate.isBefore(date));
    }

    public BigDecimal getTotalValue() {
        return employeeValue.add(employerValue);
    }
}