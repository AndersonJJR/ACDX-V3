package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmploymentContract(
    Long id,
    UUID uuid,
    Long employeeId,
    Regime regime,
    BigDecimal salaryBase,
    BigDecimal salaryHourly,
    WorkJourney workJourney,
    BigDecimal weeklyHours,
    BigDecimal dailyHours,
    String positionTitle,
    String positionCbo,
    Long departmentId,
    Long costCenterId,
    Long reportingToId,
    WorkLocation workLocation,
    String workCity,
    String workState,
    Long unionId,
    Long collectiveAgreementId,
    LocalDate probationEndDate,
    LocalDate contractStartDate,
    LocalDate contractEndDate,
    int renewalCount,
    boolean fgtsOptIn,
    String fgtsAccount,
    boolean benefitTransportVoucher,
    boolean benefitMealVoucher,
    boolean benefitHealthInsurance,
    boolean benefitDentalInsurance,
    boolean benefitLifeInsurance
) {
    public enum Regime {
        CLT, STATUTORY, APPRENTICE
    }

    public enum WorkJourney {
        FULL_TIME, PART_TIME_MORNING, PART_TIME_AFTERNOON, PART_TIME_NIGHT, SHIFT_12X36, SHIFT_6X1, CUSTOM
    }

    public enum WorkLocation {
        ONSITE, REMOTE, HYBRID
    }

    public boolean isActive(LocalDate referenceDate) {
        return !referenceDate.isBefore(contractStartDate) &&
               (contractEndDate == null || !referenceDate.isAfter(contractEndDate));
    }

    public BigDecimal calculateHourlyRate() {
        if (weeklyHours == null || weeklyHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return salaryBase.divide(weeklyHours.multiply(new BigDecimal("52")).divide(new BigDecimal("12")).divide(new BigDecimal("220"), 4, BigDecimal.ROUND_HALF_UP), 4, BigDecimal.ROUND_HALF_UP);
    }
}