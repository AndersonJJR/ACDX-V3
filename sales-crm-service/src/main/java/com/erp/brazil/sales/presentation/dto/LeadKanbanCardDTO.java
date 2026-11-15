package com.erp.brazil.sales.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeadKanbanCardDTO(
        UUID id,
        String leadCode,
        String title,
        String customerName,
        String salesPersonName,
        Integer score,
        BigDecimal estimatedValue,
        LocalDateTime expectedCloseDate
) {}