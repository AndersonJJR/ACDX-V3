package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdatePipelineRequest(
        @Size(max = 20) String code,
        @Size(max = 100) String name,
        @Size(max = 500) String description,
        Boolean isActive,
        Boolean isDefault,
        UUID salesTeamId
) {}