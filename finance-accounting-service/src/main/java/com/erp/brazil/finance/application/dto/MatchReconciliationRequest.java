package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MatchReconciliationRequest(
    @NotNull UUID statementLineId,
    @NotNull UUID glEntryId,
    @NotNull @Size(max = 200) String matchedBy,
    @Size(max = 500) String notes
) {}