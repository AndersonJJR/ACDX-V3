package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balance",
    uniqueConstraints = @UniqueConstraint(name = "uk_leave_balance_employee_type_year", columnNames = {"employee_id", "leave_type_id", "reference_year"}),
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
    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @NotNull
    @Column(name = "accrued_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal accruedDays = BigDecimal.ZERO;

    @NotNull
    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @NotNull
    @Column(name = "pending_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal pendingDays = BigDecimal.ZERO;

    @NotNull
    @Column(name = "carried_over_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal carriedOverDays = BigDecimal.ZERO;

    @NotNull
    @Column(name = "expired_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal expiredDays = BigDecimal.ZERO;

    @Column(name = "available_days", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal availableDays;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    public BigDecimal getAvailableDaysCalculated() {
        return accruedDays.add(carriedOverDays)
            .subtract(usedDays)
            .subtract(pendingDays)
            .subtract(expiredDays);
    }

    public void addAccruedDays(BigDecimal days) {
        this.accruedDays = this.accruedDays.add(days);
        this.lastAccrualDate = LocalDate.now();
    }

    public void addUsedDays(BigDecimal days) {
        this.usedDays = this.usedDays.add(days);
    }

    public void addPendingDays(BigDecimal days) {
        this.pendingDays = this.pendingDays.add(days);
    }

    public void confirmPendingDays(BigDecimal days) {
        this.pendingDays = this.pendingDays.subtract(days);
        this.usedDays = this.usedDays.add(days);
    }

    public void cancelPendingDays(BigDecimal days) {
        this.pendingDays = this.pendingDays.subtract(days);
    }

    public boolean hasSufficientBalance(BigDecimal requestedDays) {
        return getAvailableDaysCalculated().compareTo(requestedDays) >= 0;
    }
}