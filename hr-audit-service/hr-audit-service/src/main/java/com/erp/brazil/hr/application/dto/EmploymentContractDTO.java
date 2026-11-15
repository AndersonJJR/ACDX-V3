package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.EmploymentContract;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmploymentContractDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long employeeId,
    @NotNull EmploymentContract.Regime regime,
    @NotNull @DecimalMin("0.01") BigDecimal salaryBase,
    BigDecimal salaryHourly,
    @NotNull EmploymentContract.WorkJourney workJourney,
    @NotNull @DecimalMin("0.01") BigDecimal weeklyHours,
    @NotNull @DecimalMin("0.01") BigDecimal dailyHours,
    @NotBlank @Size(max = 150) String positionTitle,
    @Size(max = 10) String positionCbo,
    Long departmentId,
    Long costCenterId,
    Long reportingToId,
    EmploymentContract.WorkLocation workLocation,
    String workCity,
    String workState,
    Long unionId,
    Long collectiveAgreementId,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate probationEndDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate contractStartDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate contractEndDate,
    int renewalCount,
    boolean fgtsOptIn,
    String fgtsAccount,
    boolean benefitTransportVoucher,
    boolean benefitMealVoucher,
    boolean benefitHealthInsurance,
    boolean benefitDentalInsurance,
    boolean benefitLifeInsurance
) {
    public static EmploymentContractDTO from(EmploymentContract contract) {
        return new EmploymentContractDTO(
            contract.uuid(), contract.employeeId(), contract.regime(), contract.salaryBase(),
            contract.salaryHourly(), contract.workJourney(), contract.weeklyHours(), contract.dailyHours(),
            contract.positionTitle(), contract.positionCbo(), contract.departmentId(), contract.costCenterId(),
            contract.reportingToId(), contract.workLocation(), contract.workCity(), contract.workState(),
            contract.unionId(), contract.collectiveAgreementId(), contract.probationEndDate(),
            contract.contractStartDate(), contract.contractEndDate(), contract.renewalCount(),
            contract.fgtsOptIn(), contract.fgtsAccount(), contract.benefitTransportVoucher(),
            contract.benefitMealVoucher(), contract.benefitHealthInsurance(), contract.benefitDentalInsurance(),
            contract.benefitLifeInsurance()
        );
    }
}