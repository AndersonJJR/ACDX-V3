package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "leave_balance",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_balance_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_leave_balance_employee_type_year", columnNames = {"employee_id", "leave_type_id", "reference_year"})
    },
    indexes = @Index(name = "idx_leave_balance_employee_year", columnList = "employee_id,reference_year"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @NotNull
    @Min(2000)
    @Max(2100)
    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "accrued_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal accruedDays = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "pending_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal pendingDays = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "carried_over_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal carriedOverDays = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "expired_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal expiredDays = BigDecimal.ZERO;

    @Column(name = "available_days", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal availableDays;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    public BigDecimal getAvailableDays() {
        return accruedDays.add(carriedOverDays).subtract(usedDays).subtract(pendingDays).subtract(expiredDays);
    }

    public boolean hasAvailableDays(BigDecimal requestedDays) {
        return getAvailableDays().compareTo(requestedDays) >= 0;
    }
}