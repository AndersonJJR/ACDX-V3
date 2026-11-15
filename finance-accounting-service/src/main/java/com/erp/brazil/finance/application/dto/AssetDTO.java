package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.AssetStatus;
import com.erp.brazil.finance.domain.model.DepreciationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AssetDTO(
    UUID id,
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 100) String assetTag,
    @NotBlank @Size(max = 200) String name,
    @Size(max = 1000) String description,
    @NotNull UUID categoryId,
    UUID locationId,
    UUID costCenterId,
    @NotNull UUID accountId,
    @NotNull UUID accumulatedDepreciationAccountId,
    @NotNull UUID depreciationExpenseAccountId,
    @NotNull LocalDate acquisitionDate,
    LocalDate activationDate,
    @NotNull BigDecimal acquisitionCost,
    BigDecimal residualValue,
    @NotNull Integer usefulLifeMonths,
    @NotNull DepreciationMethod depreciationMethod,
    AssetStatus status,
    LocalDate depreciationStartDate,
    LocalDate lastDepreciationDate,
    LocalDate disposalDate,
    BigDecimal disposalValue,
    String disposalReason,
    BigDecimal accumulatedDepreciation,
    BigDecimal bookValue
) {}