package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankStatementLineDTO(
    UUID id,
    UUID statementId,
    @NotNull LocalDate transactionDate,
    @NotNull LocalDate valueDate,
    @Size(max = 500) String description,
    @NotNull BigDecimal amount,
    @NotNull TransactionType type,
    @Size(max = 20) String cnabCode,
    @Size(max = 100) String documentNumber,
    @Size(max = 100) String payerPayee,
    UUID matchedEntryId,
    java.time.LocalDateTime matchedAt,
    String matchedBy
) {}