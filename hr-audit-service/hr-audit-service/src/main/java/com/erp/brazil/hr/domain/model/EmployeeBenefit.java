package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_benefit",
    uniqueConstraints = @UniqueConstraint(name = "uk_employee_benefit_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_employee_benefit_employee", columnList = "employee_id"),
        @Index(name = "idx_employee_benefit_dates", columnList = "start_date,end_date")
    })
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
    @DecimalMin("0.00")
    @Column(name = "employee_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal employeeValue = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "employer_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal employerValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Size(max = 500)
    @Column(name = "observations", length = 500)
    private String observations;

    public enum Status {
        ACTIVE, SUSPENDED, CANCELLED, EXPIRED
    }
}