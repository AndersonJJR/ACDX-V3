package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateSalesTeamRequest(
        @Size(max = 20) String code,
        @Size(max = 100) String name,
        @Size(max = 500) String description,
        UUID managerId,
        Boolean isActive,
        List<UUID> memberIds
) {}