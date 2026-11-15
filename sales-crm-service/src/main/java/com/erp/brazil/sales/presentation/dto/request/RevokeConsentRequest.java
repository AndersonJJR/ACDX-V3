package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevokeConsentRequest(
        @NotNull @Size(max = 500) String reason
) {}