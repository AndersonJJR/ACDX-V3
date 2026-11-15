package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RunDepreciationRequest(
    @NotNull LocalDate periodEndDate
) {}