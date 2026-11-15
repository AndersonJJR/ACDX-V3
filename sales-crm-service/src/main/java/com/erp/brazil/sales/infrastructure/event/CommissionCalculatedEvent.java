package com.erp.brazil.sales.infrastructure.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionCalculatedEvent(
        UUID paymentId,
        UUID companyId,
        UUID salesPersonId,
        String salesPersonName,
        UUID commissionRuleId,
        String commissionRuleCode,
        BigDecimal amount,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDateTime calculatedAt
) {}