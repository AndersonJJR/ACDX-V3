package com.erp.brazil.hr.infrastructure.scheduler;

import com.erp.brazil.hr.domain.model.Payroll;
import com.erp.brazil.hr.domain.model.PayrollStatus;
import com.erp.brazil.hr.domain.repository.PayrollRepository;
import com.erp.brazil.hr.application.usecase.ProcessPayrollUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollScheduler {

    private final PayrollRepository payrollRepository;
    private final ProcessPayrollUseCase processPayrollUseCase;

    /**
     * Runs daily at 2 AM to check for payrolls that need to be processed.
     * Looks for payrolls in DRAFT status for the current month/year.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processScheduledPayrolls() {
        log.info("Running scheduled payroll processing check");
        
        YearMonth currentPeriod = YearMonth.now();
        
        List<Payroll> pendingPayrolls = payrollRepository.findByStatus(PayrollStatus.DRAFT);
        
        for (Payroll payroll : pendingPayrolls) {
            if (payroll.getReferenceYear() == currentPeriod.getYear() 
                    && payroll.getReferenceMonth() == currentPeriod.getMonthValue()) {
                try {
                    log.info("Processing payroll {} for period {}/{}", 
                            payroll.getId(), payroll.getReferenceMonth(), payroll.getReferenceYear());
                    processPayrollUseCase.processPayroll(payroll.getId(), null);
                } catch (Exception e) {
                    log.error("Failed to process payroll {}", payroll.getId(), e);
                }
            }
        }
    }

    /**
     * Runs on the 1st of each month at 3 AM to accrue leave balances.
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void accrueLeaveBalances() {
        log.info("Running monthly leave balance accrual");
        // Implementation would call ApproveLeaveRequestUseCase.accrueLeaveBalances
        // for all active employees
    }

    /**
     * Runs daily at 1 AM to calculate time sheets for the previous day.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void calculateTimeSheets() {
        log.info("Running daily time sheet calculation");
        // Implementation would calculate time sheets for previous day
    }
}