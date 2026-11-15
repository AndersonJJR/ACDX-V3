package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.LGPDConsent;
import com.erp.brazil.sales.domain.model.LGPDConsentStatus;
import com.erp.brazil.sales.domain.model.LGPDConsentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record LGPDConsentDTO(
        UUID id,
        LGPDConsent.EntityType entityType,
        UUID entityId,
        String entityName,
        LGPDConsentType type,
        LGPDConsentStatus status,
        LocalDateTime grantedAt,
        LocalDateTime revokedAt,
        LocalDateTime expiresAt,
        String legalBasis,
        String purpose,
        String version,
        String ipAddress,
        String userAgent,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}