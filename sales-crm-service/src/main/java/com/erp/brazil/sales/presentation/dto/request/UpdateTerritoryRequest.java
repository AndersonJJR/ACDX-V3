package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateTerritoryRequest(
        @Size(max = 20) String code,
        @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Size(max = 2) String state,
        List<String> cities,
        List<String> zipCodeRanges,
        Boolean active,
        UUID parentTerritoryId
) {}