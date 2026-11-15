package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class ProcessPayrollUseCase {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentContractRepository contractRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final BenefitRepository benefitRepository;
    private final CalculateINSSUseCase inssUseCase;
    private final CalculateIRRFUseCase irrfUseCase;
    private final CalculateFGTSUseCase fgtsUseCase;
    private final GeneratePayslipUseCase generatePayslipUseCase;
    private final AuditEventUseCase auditEventUseCase;

    public ProcessPayrollUseCase(PayrollRepository payrollRepository, PayrollItemRepository payrollItemRepository,
                                  PayslipRepository payslipRepository, EmployeeRepository employeeRepository,
                                  EmploymentContractRepository contractRepository, TimeSheetRepository timeSheetRepository,
                                  LeaveBalanceRepository leaveBalanceRepository, BenefitRepository benefitRepository,
                                  CalculateINSSUseCase inssUseCase, CalculateIRRFUseCase irrfUseCase,
                                  CalculateFGTSUseCase fgtsUseCase, GeneratePayslipUseCase generatePayslipUseCase,
                                  AuditEventUseCase auditEventUseCase) {
        this.payrollRepository = payrollRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.payslipRepository = payslipRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.timeSheetRepository = timeSheetRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.benefitRepository = benefitRepository;
        this.inssUseCase = inssUseCase;
        this.irrfUseCase = irrfUseCase;
        this.fgtsUseCase = fgtsUseCase;
        this.generatePayslipUseCase = generatePayslipUseCase;
        this.auditEventUseCase = auditEventUseCase;
    }

    @Transactional
    public CompletableFuture<Payroll> processAsync(Long payrollId) {
        return CompletableFuture.supplyAsync(() -> process(payrollId));
    }

    @Transactional
    public Payroll process(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + payrollId));

        payroll = payrollRepository.save(new Payroll(
            payroll.id(), payroll.uuid(), payroll.companyId(), payroll.referenceMonth(), payroll.referenceYear(),
            Payroll.PayrollStatus.CALCULATING, payroll.totalEmployees(), payroll.totalGrossAmount(),
            payroll.totalNetAmount(), payroll.totalInssAmount(), payroll.totalIrrfAmount(),
            payroll.totalFgtsAmount(), payroll.totalBenefitsAmount(), payroll.totalDeductionsAmount(),
            payroll.calculatedAt(), payroll.calculatedBy(), payroll.approvedAt(), payroll.approvedBy(),
            payroll.processedAt(), payroll.processedBy(), payroll.paidAt(), payroll.paidBy(),
            payroll.errorMessage(), payroll.observations()
        ));

        try {
            List<Employee> employees = employeeRepository.findByCompanyIdAndStatus(
                payroll.companyId(), Employee.EmployeeStatus.ACTIVE);

            BigDecimal totalGross = BigDecimal.ZERO;
            BigDecimal totalNet = BigDecimal.ZERO;
            BigDecimal totalInss = BigDecimal.ZERO;
            BigDecimal totalIrrf = BigDecimal.ZERO;
            BigDecimal totalFgts = BigDecimal.ZERO;
            BigDecimal totalBenefits = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;

            for (Employee employee : employees) {
                Payslip payslip = calculateEmployeePayroll(payroll, employee);
                totalGross = totalGross.add(payslip.grossAmount());
                totalNet = totalNet.add(payslip.netAmount());
                totalInss = totalInss.add(payslip.inssAmount());
                totalIrrf = totalIrrf.add(payslip.irrfAmount());
                totalFgts = totalFgts.add(payslip.fgtsAmount());
                totalBenefits = totalBenefits.add(payslip.grossAmount().subtract(payslip.netAmount()).subtract(payslip.inssAmount()).subtract(payslip.irrfAmount()));
                totalDeductions = totalDeductions.add(payslip.inssAmount()).add(payslip.irrfAmount());
            }

            Payroll finalPayroll = new Payroll(
                payroll.id(), payroll.uuid(), payroll.companyId(), payroll.referenceMonth(), payroll.referenceYear(),
                Payroll.PayrollStatus.CALCULATED, employees.size(), totalGross, totalNet, totalInss, totalIrrf,
                totalFgts, totalBenefits, totalDeductions, LocalDateTime.now(), payroll.calculatedBy(),
                payroll.approvedAt(), payroll.approvedBy(), payroll.processedAt(), payroll.processedBy(),
                payroll.paidAt(), payroll.paidBy(), null, payroll.observations()
            );

            payrollRepository.save(finalPayroll);
            auditEventUseCase.logEvent("PAYROLL_CALCULATED", "Payroll", payrollId.toString(), payroll.companyId(), null, finalPayroll);

            return finalPayroll;
        } catch (Exception e) {
            Payroll errorPayroll = new Payroll(
                payroll.id(), payroll.uuid(), payroll.companyId(), payroll.referenceMonth(), payroll.referenceYear(),
                Payroll.PayrollStatus.ERROR, payroll.totalEmployees(), payroll.totalGrossAmount(),
                payroll.totalNetAmount(), payroll.totalInssAmount(), payroll.totalIrrfAmount(),
                payroll.totalFgtsAmount(), payroll.totalBenefitsAmount(), payroll.totalDeductionsAmount(),
                payroll.calculatedAt(), payroll.calculatedBy(), payroll.approvedAt(), payroll.approvedBy(),
                payroll.processedAt(), payroll.processedBy(), payroll.paidAt(), payroll.paidBy(),
                e.getMessage(), payroll.observations()
            );
            payrollRepository.save(errorPayroll);
            throw new RuntimeException("Payroll processing failed", e);
        }
    }

    private Payslip calculateEmployeePayroll(Payroll payroll, Employee employee) {
        EmploymentContract contract = contractRepository.findActiveByEmployeeIdAndDate(
            employee.id(), LocalDate.of(payroll.referenceYear(), payroll.referenceMonth(), 1))
            .orElseThrow(() -> new IllegalStateException("No active contract for employee: " + employee.id()));

        List<PayrollItem> items = new ArrayList<>();
        BigDecimal salaryBase = contract.salaryBase();

        // Base salary
        items.add(PayrollItem.builder()
            .payrollId(payroll.id()).employeeId(employee.id())
            .category(PayrollItem.Category.SALARY_BASE).itemType(PayrollItem.ItemType.EARNING)
            .code("SAL_BASE").description("Base Salary")
            .totalAmount(salaryBase).referenceValue(salaryBase).quantity(BigDecimal.ONE)
            .taxableInss(true).taxableIrrf(true).taxableFgts(true).isFixed(true)
            .calculationBasis("Contract base salary").displayOrder(1).build());

        // Overtime from timesheet
        TimeSheet timeSheet = timeSheetRepository.findByEmployeeIdAndReferenceMonthAndReferenceYear(
            employee.id(), payroll.referenceMonth(), payroll.referenceYear()).orElse(null);
        if (timeSheet != null && timeSheet.totalOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourlyRate = contract.calculateHourlyRate();
            BigDecimal overtimeAmount = timeSheet.totalOvertimeHours().multiply(hourlyRate).multiply(new BigDecimal("1.5"));
            items.add(PayrollItem.builder()
                .payrollId(payroll.id()).employeeId(employee.id())
                .category(PayrollItem.Category.OVERTIME).itemType(PayrollItem.ItemType.EARNING)
                .code("OVERTIME").description("Overtime (50%)")
                .totalAmount(overtimeAmount).referenceValue(hourlyRate).quantity(timeSheet.totalOvertimeHours().multiply(new BigDecimal("1.5")))
                .taxableInss(true).taxableIrrf(true).taxableFgts(true)
                .calculationBasis("Hours: " + timeSheet.totalOvertimeHours() + " x Rate: " + hourlyRate + " x 1.5").displayOrder(2).build());
        }

        // Night shift
        if (timeSheet != null && timeSheet.totalNightHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourlyRate = contract.calculateHourlyRate();
            BigDecimal nightAmount = timeSheet.totalNightHours().multiply(hourlyRate).multiply(new BigDecimal("0.20"));
            items.add(PayrollItem.builder()
                .payrollId(payroll.id()).employeeId(employee.id())
                .category(PayrollItem.Category.NIGHT_SHIFT).itemType(PayrollItem.ItemType.EARNING)
                .code("NIGHT_SHIFT").description("Night Shift Additional (20%)")
                .totalAmount(nightAmount).referenceValue(hourlyRate).quantity(timeSheet.totalNightHours().multiply(new BigDecimal("0.20")))
                .taxableInss(true).taxableIrrf(true).taxableFgts(true)
                .calculationBasis("Hours: " + timeSheet.totalNightHours() + " x Rate: " + hourlyRate + " x 0.20").displayOrder(3).build());
        }

        // DSR (Descanso Semanal Remunerado) - included in base

        // Benefits
        if (contract.benefitTransportVoucher()) {
            BigDecimal transportValue = salaryBase.multiply(new BigDecimal("0.06")).min(new BigDecimal("300.00"));
            items.add(PayrollItem.builder()
                .payrollId(payroll.id()).employeeId(employee.id())
                .category(PayrollItem.Category.TRANSPORT_VOUCHER).itemType(PayrollItem.ItemType.DEDUCTION)
                .code("TRANSPORT").description("Transport Voucher (6% up to R$ 300)")
                .totalAmount(transportValue).taxableInss(false).taxableIrrf(false)
                .calculationBasis("Base: " + salaryBase + " x 6%").displayOrder(10).build());
        }

        if (contract.benefitMealVoucher()) {
            BigDecimal mealValue = new BigDecimal("25.00").multiply(BigDecimal.valueOf(22)); // 22 working days
            items.add(PayrollItem.builder()
                .payrollId(payroll.id()).employeeId(employee.id())
                .category(PayrollItem.Category.MEAL_VOUCHER).itemType(PayrollItem.ItemType.EARNING)
                .code("MEAL").description("Meal Voucher")
                .totalAmount(mealValue).taxableInss(false).taxableIrrf(false)
                .calculationBasis("Daily: R$ 25.00 x 22 days").displayOrder(11).build());
        }

        // INSS
        BigDecimal inssBase = items.stream()
            .filter(i -> i.taxableInss())
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        inssUseCase.applyToPayrollItems(items, inssBase);

        // IRRF
        BigDecimal irrfBase = items.stream()
            .filter(i -> i.taxableIrrf())
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int dependents = 0; // Would fetch from dependents
        irrfUseCase.applyToPayrollItems(items, irrfBase, dependents);

        // FGTS
        BigDecimal fgtsBase = items.stream()
            .filter(i -> i.taxableFgts())
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        fgtsUseCase.applyToPayrollItems(items, fgtsBase);

        // Save items
        for (PayrollItem item : items) {
            payrollItemRepository.save(item);
        }

        // Calculate totals
        BigDecimal grossAmount = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.EARNING)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deductions = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.DEDUCTION)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal employerCosts = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.EMPLOYER_COST)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = grossAmount.subtract(deductions);
        BigDecimal inssAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.INSS_EMPLOYEE)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal irrfAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.IRRF)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fgtsAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.FGTS)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate payslip
        Payslip payslip = generatePayslipUseCase.generate(payroll.id(), employee.id(), items);

        return new Payslip(
            payslip.id(), payslip.uuid(), payslip.payrollId(), payslip.employeeId(),
            grossAmount, netAmount, inssBase, inssAmount, irrfBase, irrfAmount,
            fgtsBase, fgtsAmount, payslip.fgtsDepositDate(), payslip.pdfPath(),
            payslip.pdfGeneratedAt(), payslip.digitalSignature(), payslip.signedAt(),
            payslip.deliveredAt(), payslip.deliveryMethod(), payslip.status(),
            payslip.accessToken(), payslip.accessExpiresAt()
        );
    }
}