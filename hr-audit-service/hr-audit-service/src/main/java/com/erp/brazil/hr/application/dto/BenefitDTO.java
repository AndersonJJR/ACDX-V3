package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Benefit;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BenefitDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long companyId,
    @NotBlank @Size(max = 20) String code,
    @NotBlank @Size(max = 100) String name,
    String description,
    @NotNull Benefit.BenefitType benefitType,
    String providerName,
    String providerCnpj,
    String planName,
    String planNumber,
    BigDecimal employeeContributionPct,
    BigDecimal employerContributionPct,
    BigDecimal maxEmployeeValue,
    BigDecimal maxEmployerValue,
    boolean isMandatory,
    boolean isTaxable,
    boolean requiresDependent,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
    boolean isActive
) {
    public static BenefitDTO from(Benefit benefit) {
        return new BenefitDTO(
            benefit.uuid(), benefit.companyId(), benefit.code(), benefit.name(), benefit.description(),
            benefit.benefitType(), benefit.providerName(), benefit.providerCnpj(), benefit.planName(),
            benefit.planNumber(), benefit.employeeContributionPct(), benefit.employerContributionPct(),
            benefit.maxEmployeeValue(), benefit.maxEmployerValue(), benefit.isMandatory(), benefit.isTaxable(),
            benefit.requiresDependent(), benefit.startDate(), benefit.endDate(), benefit.isActive()
        );
    }
}