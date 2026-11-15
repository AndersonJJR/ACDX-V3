package com.erp.brazil.sales.infrastructure.scheduler;

import com.erp.brazil.sales.application.usecase.CalculateCommissionUseCase;
import com.erp.brazil.sales.presentation.dto.request.CalculateCommissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Component
public class CommissionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CommissionScheduler.class);

    private final CalculateCommissionUseCase calculateCommissionUseCase;

    public CommissionScheduler(CalculateCommissionUseCase calculateCommissionUseCase) {
        this.calculateCommissionUseCase = calculateCommissionUseCase;
    }

    @Scheduled(cron = "${commission.schedule.cron:0 0 2 1 * ?}")
    public void calculateMonthlyCommissions() {
        log.info("Starting monthly commission calculation job");

        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate periodStart = previousMonth.atDay(1);
        LocalDate periodEnd = previousMonth.atEndOfMonth();

        UUID systemUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try {
            CalculateCommissionRequest request = new CalculateCommissionRequest(
                    periodStart, periodEnd, systemUserId
            );
            calculateCommissionUseCase.execute(request);
            log.info("Monthly commission calculation completed for period: {} to {}", periodStart, periodEnd);
        } catch (Exception e) {
            log.error("Failed to calculate monthly commissions", e);
        }
    }
}