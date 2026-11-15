package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.PayslipRepository;
import com.erp.brazil.hr.infrastructure.pdf.PayslipPDFGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class GeneratePayslipUseCase {

    private final PayslipRepository payslipRepository;
    private final PayslipPDFGenerator pdfGenerator;

    public GeneratePayslipUseCase(PayslipRepository payslipRepository, PayslipPDFGenerator pdfGenerator) {
        this.payslipRepository = payslipRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @Transactional
    public Payslip generate(Long payrollId, Long employeeId, List<PayrollItem> items) {
        Optional<Payslip> existing = payslipRepository.findByPayrollIdAndEmployeeId(payrollId, employeeId);
        if (existing.isPresent()) {
            return existing.get();
        }

        BigDecimal grossAmount = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.EARNING)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deductions = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.DEDUCTION)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = grossAmount.subtract(deductions);

        BigDecimal inssBase = items.stream()
            .filter(PayrollItem::taxableInss)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal inssAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.INSS_EMPLOYEE)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal irrfBase = items.stream()
            .filter(PayrollItem::taxableIrrf)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal irrfAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.IRRF)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fgtsBase = items.stream()
            .filter(PayrollItem::taxableFgts)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fgtsAmount = items.stream()
            .filter(i -> i.category() == PayrollItem.Category.FGTS)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String pdfPath = pdfGenerator.generate(payrollId, employeeId, items);
        String accessToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(90);

        Payslip payslip = new Payslip(
            null, UUID.randomUUID(), payrollId, employeeId, grossAmount, netAmount,
            inssBase, inssAmount, irrfBase, irrfAmount, fgtsBase, fgtsAmount,
            LocalDate.now(), pdfPath, LocalDateTime.now(), null, null, null,
            Payslip.DeliveryMethod.PORTAL, Payslip.PayslipStatus.GENERATED,
            accessToken, expiresAt
        );

        return payslipRepository.save(payslip);
    }

    @Transactional
    public Payslip sign(Long payslipId, String digitalSignature) {
        Payslip payslip = payslipRepository.findById(payslipId)
            .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + payslipId));

        Payslip signed = new Payslip(
            payslip.id(), payslip.uuid(), payslip.payrollId(), payslip.employeeId(),
            payslip.grossAmount(), payslip.netAmount(), payslip.inssBase(), payslip.inssAmount(),
            payslip.irrfBase(), payslip.irrfAmount(), payslip.fgtsBase(), payslip.fgtsAmount(),
            payslip.fgtsDepositDate(), payslip.pdfPath(), payslip.pdfGeneratedAt(),
            digitalSignature, LocalDateTime.now(), payslip.deliveredAt(),
            payslip.deliveryMethod(), Payslip.PayslipStatus.SIGNED,
            payslip.accessToken(), payslip.accessExpiresAt()
        );

        return payslipRepository.save(signed);
    }
}