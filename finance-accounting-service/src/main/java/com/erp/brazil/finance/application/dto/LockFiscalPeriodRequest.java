package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LockFiscalPeriodRequest(
    @NotNull LocalDate lockDate,
    @NotBlank @Size(max = 200) String lockedBy
) {}