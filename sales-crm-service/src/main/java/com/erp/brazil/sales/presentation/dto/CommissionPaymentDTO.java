package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.CommissionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionPaymentDTO(
        UUID id,
        UUID companyId,
        UUID commissionRuleId,
        String commissionRuleCode,
        UUID salesPersonId,
        String salesPersonName,
        UUID salesOrderId,
        String salesOrderNumber,
        UUID salesInvoiceId,
        String salesInvoiceNumber,
        BigDecimal baseAmount,
        BigDecimal commissionRate,
        BigDecimal amount,
        CommissionStatus status,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDateTime calculatedAt,
        UUID calculatedById,
        String calculatedByName,
        LocalDateTime paidAt,
        UUID paidById,
        String paidByName,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}