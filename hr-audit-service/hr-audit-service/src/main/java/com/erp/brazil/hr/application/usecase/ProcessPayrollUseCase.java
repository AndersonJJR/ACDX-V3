package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPayrollUseCase {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentContractRepository contractRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final BenefitRepository benefitRepository;
    private final EmployeeBenefitRepository employeeBenefitRepository;
    private final HolidayRepository holidayRepository;
    private final CollectiveAgreementRepository collectiveAgreementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Payroll calculatePayroll(Long companyId, Integer month, Integer year) {
        log.info("Starting payroll calculation for company {} period {}/{}", companyId, month, year);

        Payroll payroll = payrollRepository.findByCompanyIdAndReferenceMonthAndReferenceYear(companyId, month, year)
            .orElseGet(() -> createNewPayroll(companyId, month, year));

        if (payroll.getStatus() != PayrollStatus.DRAFT && payroll.getStatus() != PayrollStatus.ERROR) {
            throw new IllegalStateException("Payroll already processed or in progress");
        }

        payroll.setStatus(PayrollStatus.CALCULATING);
        payrollRepository.save(payroll);

        try {
            List<Employee> employees = employeeRepository.findByCompanyIdAndStatusIn(companyId,
                List.of(EmployeeStatus.ACTIVE, EmployeeStatus.ON_LEAVE));

            payroll.setTotalEmployees(employees.size());

            BigDecimal totalGross = BigDecimal.ZERO;
            BigDecimal totalNet = BigDecimal.ZERO;
            BigDecimal totalInss = BigDecimal.ZERO;
            BigDecimal totalIrrf = BigDecimal.ZERO;
            BigDecimal totalFgts = BigDecimal.ZERO;
            BigDecimal totalBenefits = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;

            for (Employee employee : employees) {
                PayrollCalculationResult result = calculateEmployeePayroll(employee, payroll, month, year);
                totalGross = totalGross.add(result.grossAmount());
                totalNet = totalNet.add(result.netAmount());
                totalInss = totalInss.add(result.inssAmount());
                totalIrrf = totalIrrf.add(result.irrfAmount());
                totalFgts = totalFgts.add(result.fgtsAmount());
                totalBenefits = totalBenefits.add(result.benefitsAmount());
                totalDeductions = totalDeductions.add(result.deductionsAmount());
            }

            payroll.setTotalGrossAmount(totalGross);
            payroll.setTotalNetAmount(totalNet);
            payroll.setTotalInssAmount(totalInss);
            payroll.setTotalIrrfAmount(totalIrrf);
            payroll.setTotalFgtsAmount(totalFgts);
            payroll.setTotalBenefitsAmount(totalBenefits);
            payroll.setTotalDeductionsAmount(totalDeductions);
            payroll.setStatus(PayrollStatus.CALCULATED);
            payroll.setCalculatedAt(java.time.Instant.now());

            payrollRepository.save(payroll);

            log.info("Payroll calculated successfully for company {} period {}/{}: {} employees, gross: {}, net: {}",
                companyId, month, year, employees.size(), totalGross, totalNet);

            return payroll;

        } catch (Exception e) {
            log.error("Error calculating payroll", e);
            payroll.setStatus(PayrollStatus.ERROR);
            payroll.setErrorMessage(e.getMessage());
            payrollRepository.save(payroll);
            throw e;
        }
    }

    private Payroll createNewPayroll(Long companyId, Integer month, Integer year) {
        Payroll payroll = new Payroll();
        payroll.setCompanyId(companyId);
        payroll.setReferenceMonth(month);
        payroll.setReferenceYear(year);
        payroll.setStatus(PayrollStatus.DRAFT);
        return payrollRepository.save(payroll);
    }

    private PayrollCalculationResult calculateEmployeePayroll(Employee employee, Payroll payroll, Integer month, Integer year) {
        EmploymentContract contract = contractRepository.findByEmployeeId(employee.getId())
            .orElseThrow(() -> new IllegalStateException("Employee has no contract: " + employee.getId()));

        BigDecimal salaryBase = contract.getSalaryBase();
        BigDecimal grossAmount = salaryBase;

        TimeSheet timeSheet = timeSheetRepository.findByEmployeeIdAndReferenceMonthAndReferenceYear(employee.getId(), month, year).orElse(null);

        BigDecimal overtimeHours = timeSheet != null ? timeSheet.getTotalOvertimeHours() : BigDecimal.ZERO;
        BigDecimal overtimeAmount = calculateOvertime(contract, overtimeHours);
        if (overtimeAmount.compareTo(BigDecimal.ZERO) > 0) {
            grossAmount = grossAmount.add(overtimeAmount);
            createPayrollItem(payroll, employee, PayrollItemType.EARNING, PayrollItemCategory.OVERTIME,
                "Horas Extras", overtimeHours, contract.getSalaryHourly(), overtimeAmount, true, true, true);
        }

        BigDecimal nightHours = timeSheet != null ? timeSheet.getTotalNightHours() : BigDecimal.ZERO;
        BigDecimal nightAmount = calculateNightShift(contract, nightHours);
        if (nightAmount.compareTo(BigDecimal.ZERO) > 0) {
            grossAmount = grossAmount.add(nightAmount);
            createPayrollItem(payroll, employee, PayrollItemType.EARNING, PayrollItemCategory.NIGHT_SHIFT,
                "Adicional Noturno", nightHours, contract.getSalaryHourly().multiply(BigDecimal.valueOf(0.2)), nightAmount, true, true, true);
        }

        BigDecimal inssBase = calculateInssBase(grossAmount);
        BigDecimal inssAmount = calculateInss(inssBase);
        createPayrollItem(payroll, employee, PayrollItemType.DEDUCTION, PayrollItemCategory.INSS_EMPLOYEE,
            "INSS", inssBase, BigDecimal.ONE, inssAmount, true, false, false);

        BigDecimal irrfBase = calculateIrrfBase(grossAmount, inssAmount, employee);
        BigDecimal irrfAmount = calculateIrrf(irrfBase, employee);
        createPayrollItem(payroll, employee, PayrollItemType.DEDUCTION, PayrollItemCategory.IRRF,
            "IRRF", irrfBase, BigDecimal.ONE, irrfAmount, false, true, false);

        BigDecimal fgtsBase = calculateFgtsBase(grossAmount);
        BigDecimal fgtsAmount = calculateFgts(fgtsBase);
        createPayrollItem(payroll, employee, PayrollItemType.EMPLOYER_COST, PayrollItemCategory.FGTS,
            "FGTS", fgtsBase, BigDecimal.ONE, fgtsAmount, false, false, true);

        BigDecimal inssEmployerAmount = calculateInssEmployer(fgtsBase);
        createPayrollItem(payroll, employee, PayrollItemType.EMPLOYER_COST, PayrollItemCategory.INSS_EMPLOYER,
            "INSS Patronal", fgtsBase, BigDecimal.ONE, inssEmployerAmount, false, false, false);

        BigDecimal benefitsAmount = calculateBenefits(employee, payroll);
        BigDecimal deductionsAmount = calculateDeductions(employee, payroll, inssAmount, irrfAmount);

        BigDecimal netAmount = grossAmount.subtract(inssAmount).subtract(irrfAmount).subtract(benefitsAmount).subtract(deductionsAmount);

        createPayslip(payroll, employee, grossAmount, netAmount, inssBase, inssAmount, irrfBase, irrfAmount, fgtsBase, fgtsAmount);

        return new PayrollCalculationResult(grossAmount, netAmount, inssAmount, irrfAmount, fgtsAmount, benefitsAmount, deductionsAmount);
    }

    private BigDecimal calculateOvertime(EmploymentContract contract, BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal hourlyRate = contract.getSalaryHourly();
        return hourlyRate.multiply(BigDecimal.valueOf(1.5)).multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNightShift(EmploymentContract contract, BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal hourlyRate = contract.getSalaryHourly();
        return hourlyRate.multiply(BigDecimal.valueOf(0.2)).multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInssBase(BigDecimal grossAmount) {
        return grossAmount.min(BigDecimal.valueOf(7786.02));
    }

    private BigDecimal calculateInss(BigDecimal base) {
        BigDecimal[] brackets = {BigDecimal.valueOf(1412.00), BigDecimal.valueOf(2666.68), BigDecimal.valueOf(4000.03), BigDecimal.valueOf(7786.02)};
        BigDecimal[] rates = {BigDecimal.valueOf(7.5), BigDecimal.valueOf(9.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(14.0)};
        
        BigDecimal remaining = base;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal previousLimit = BigDecimal.ZERO;

        for (int i = 0; i < brackets.length; i++) {
            BigDecimal limit = brackets[i];
            BigDecimal rate = rates[i].divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            if (remaining.compareTo(previousLimit) <= 0) break;

            BigDecimal bracketBase = remaining.min(limit).subtract(previousLimit);
            if (bracketBase.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(bracketBase.multiply(rate));
            }
            previousLimit = limit;
        }

        return total.min(BigDecimal.valueOf(908.86)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInssEmployer(BigDecimal base) {
        return base.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateIrrfBase(BigDecimal grossAmount, BigDecimal inssAmount, Employee employee) {
        int dependents = 0;
        BigDecimal dependentDeduction = BigDecimal.valueOf(189.59).multiply(BigDecimal.valueOf(dependents));
        return grossAmount.subtract(inssAmount).subtract(dependentDeduction);
    }

    private BigDecimal calculateIrrf(BigDecimal base, Employee employee) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal[] brackets = {BigDecimal.valueOf(2259.20), BigDecimal.valueOf(2826.65), BigDecimal.valueOf(3751.05), BigDecimal.valueOf(4664.68), BigDecimal.valueOf(999999999.00)};
        BigDecimal[] rates = {BigDecimal.ZERO, BigDecimal.valueOf(7.5), BigDecimal.valueOf(15.0), BigDecimal.valueOf(22.5), BigDecimal.valueOf(27.5)};
        BigDecimal[] deductions = {BigDecimal.ZERO, BigDecimal.valueOf(169.44), BigDecimal.valueOf(381.44), BigDecimal.valueOf(662.77), BigDecimal.valueOf(896.00)};

        BigDecimal remaining = base;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal previousLimit = BigDecimal.ZERO;

        for (int i = 0; i < brackets.length; i++) {
            BigDecimal limit = brackets[i];
            BigDecimal rate = rates[i].divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            if (remaining.compareTo(previousLimit) <= 0) break;

            BigDecimal bracketBase = remaining.min(limit).subtract(previousLimit);
            if (bracketBase.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(bracketBase.multiply(rate));
            }
            previousLimit = limit;
        }

        return total.subtract(deductions[getIrrfBracketIndex(base)]).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private int getIrrfBracketIndex(BigDecimal base) {
        BigDecimal[] brackets = {BigDecimal.valueOf(2259.20), BigDecimal.valueOf(2826.65), BigDecimal.valueOf(3751.05), BigDecimal.valueOf(4664.68), BigDecimal.valueOf(999999999.00)};
        for (int i = 0; i < brackets.length; i++) {
            if (base.compareTo(brackets[i]) <= 0) return i;
        }
        return brackets.length - 1;
    }

    private BigDecimal calculateFgtsBase(BigDecimal grossAmount) {
        return grossAmount;
    }

    private BigDecimal calculateFgts(BigDecimal base) {
        return base.multiply(BigDecimal.valueOf(8.0).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBenefits(Employee employee, Payroll payroll) {
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateDeductions(Employee employee, Payroll payroll, BigDecimal inssAmount, BigDecimal irrfAmount) {
        return BigDecimal.ZERO;
    }

    private void createPayrollItem(Payroll payroll, Employee employee, PayrollItemType itemType, PayrollItemCategory category,
                                   String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalAmount,
                                   boolean taxableInss, boolean taxableIrrf, boolean taxableFgts) {
        PayrollItem item = new PayrollItem();
        item.setPayroll(payroll);
        item.setEmployee(employee);
        item.setItemType(itemType);
        item.setCategory(category);
        item.setCode(category.name());
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalAmount(totalAmount);
        item.setTaxableInss(taxableInss);
        item.setTaxableIrrf(taxableIrrf);
        item.setTaxableFgts(taxableFgts);
        payrollItemRepository.save(item);
    }

    private void createPayslip(Payroll payroll, Employee employee, BigDecimal grossAmount, BigDecimal netAmount,
                               BigDecimal inssBase, BigDecimal inssAmount, BigDecimal irrfBase, BigDecimal irrfAmount,
                               BigDecimal fgtsBase, BigDecimal fgtsAmount) {
        Payslip payslip = new Payslip();
        payslip.setPayroll(payroll);
        payslip.setEmployee(employee);
        payslip.setGrossAmount(grossAmount);
        payslip.setNetAmount(netAmount);
        payslip.setInssBase(inssBase);
        payslip.setInssAmount(inssAmount);
        payslip.setIrrfBase(irrfBase);
        payslip.setIrrfAmount(irrfAmount);
        payslip.setFgtsBase(fgtsBase);
        payslip.setFgtsAmount(fgtsAmount);
        payslip.setStatus(PayslipStatus.GENERATED);
        payslipRepository.save(payslip);
    }

    @Transactional
    public Payroll approvePayroll(Long payrollId, Long approvedBy) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found"));

        if (!payroll.canTransitionTo(PayrollStatus.APPROVED)) {
            throw new IllegalStateException("Cannot approve payroll in status: " + payroll.getStatus());
        }

        payroll.transitionTo(PayrollStatus.APPROVED);
        payroll.setApprovedAt(java.time.Instant.now());
        payroll.setApprovedBy(approvedBy);
        return payrollRepository.save(payroll);
    }

    @Transactional
    public Payroll processPayroll(Long payrollId, Long processedBy) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found"));

        if (!payroll.canTransitionTo(PayrollStatus.PROCESSING)) {
            throw new IllegalStateException("Cannot process payroll in status: " + payroll.getStatus());
        }

        payroll.transitionTo(PayrollStatus.PROCESSING);
        payroll.setProcessedAt(java.time.Instant.now());
        payroll.setProcessedBy(processedBy);

        payroll.transitionTo(PayrollStatus.PROCESSED);
        Payroll saved = payrollRepository.save(payroll);

        eventPublisher.publishEvent(new PayrollProcessedEvent(
            saved.getId(),
            saved.getReferenceMonth(),
            saved.getReferenceYear(),
            saved.getTotalEmployees(),
            saved.getTotalGrossAmount(),
            saved.getTotalNetAmount(),
            saved.getTotalInssAmount(),
            saved.getTotalIrrfAmount(),
            saved.getTotalFgtsAmount(),
            saved.getPayrollItems().stream().map(PayrollItem::getEmployeeId).distinct().toList(),
            saved.getCompanyId()
        ));

        return saved;
    }

    private record PayrollCalculationResult(
        BigDecimal grossAmount,
        BigDecimal netAmount,
        BigDecimal inssAmount,
        BigDecimal irrfAmount,
        BigDecimal fgtsAmount,
        BigDecimal benefitsAmount,
        BigDecimal deductionsAmount
    ) {}
}