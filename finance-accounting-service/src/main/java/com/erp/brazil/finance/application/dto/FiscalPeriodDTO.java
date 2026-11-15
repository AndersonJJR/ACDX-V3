package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalPeriodDTO(
    UUID id,
    UUID fiscalYearId,
    Integer year,
    @NotNull Integer periodNumber,
    @Size(max = 50) String name,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    boolean locked,
    LocalDate lockedAt,
    String lockedBy,
    boolean closed,
    LocalDate closedAt
) {}