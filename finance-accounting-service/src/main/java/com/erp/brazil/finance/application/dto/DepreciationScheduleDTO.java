package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DepreciationScheduleDTO(
    UUID id,
    UUID assetId,
    @NotNull LocalDate periodStart,
    @NotNull LocalDate periodEnd,
    @NotNull BigDecimal depreciationAmount,
    @NotNull BigDecimal accumulatedDepreciation,
    @NotNull BigDecimal bookValue,
    boolean posted,
    java.time.LocalDateTime postedAt
) {}