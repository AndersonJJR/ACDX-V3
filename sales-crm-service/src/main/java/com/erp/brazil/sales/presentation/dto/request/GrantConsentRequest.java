package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.LGPDConsent;
import com.erp.brazil.sales.domain.model.LGPDConsentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record GrantConsentRequest(
        @NotNull LGPDConsent.EntityType entityType,
        @NotNull UUID entityId,
        @NotNull LGPDConsentType type,
        @NotNull String legalBasis,
        @NotNull @Size(max = 500) String purpose,
        @Size(max = 50) String version,
        LocalDateTime expiresAt,
        @Size(max = 45) String ipAddress,
        @Size(max = 500) String userAgent,
        @Size(max = 500) String notes
) {}