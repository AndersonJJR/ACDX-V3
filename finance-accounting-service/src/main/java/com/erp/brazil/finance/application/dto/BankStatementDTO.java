package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.BankStatementStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankStatementDTO(
    UUID id,
    UUID bankAccountId,
    @NotNull LocalDate periodStart,
    @NotNull LocalDate periodEnd,
    @NotNull BigDecimal initialBalance,
    @NotNull BigDecimal finalBalance,
    @Size(max = 500) String filePath,
    BankStatementStatus status,
    java.time.LocalDateTime importedAt,
    String importedBy
) {}