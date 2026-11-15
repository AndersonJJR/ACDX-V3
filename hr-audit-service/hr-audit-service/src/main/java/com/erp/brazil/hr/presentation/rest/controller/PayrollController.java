package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.application.usecase.ProcessPayrollUseCase;
import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.presentation.rest.dto.PayrollDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll processing APIs")
public class PayrollController {

    private final ProcessPayrollUseCase payrollUseCase;
    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayslipRepository payslipRepository;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Calculate payroll for period")
    public ResponseEntity<PayrollDto.PayrollResponse> calculate(@Valid @RequestBody PayrollDto.PayrollRequest request) {
        Payroll payroll = payrollUseCase.calculatePayroll(
            getCurrentCompanyId(),
            request.getReferenceMonth(),
            request.getReferenceYear()
        );
        return ResponseEntity.ok(toPayrollResponse(payroll));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Approve payroll")
    public ResponseEntity<PayrollDto.PayrollResponse> approve(@PathVariable Long id) {
        Payroll payroll = payrollUseCase.approvePayroll(id, getCurrentUserId());
        return ResponseEntity.ok(toPayrollResponse(payroll));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Process payroll (generate PDFs)")
    public ResponseEntity<PayrollDto.PayrollResponse> process(@PathVariable Long id) {
        Payroll payroll = payrollUseCase.processPayroll(id, getCurrentUserId());
        return ResponseEntity.ok(toPayrollResponse(payroll));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "List payrolls")
    public ResponseEntity<PayrollDto.PageResponse> list(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PayrollStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("referenceYear").descending().and(Sort.by("referenceMonth").descending()));
        Page<Payroll> payrolls = status != null ?
            payrollRepository.findByCompanyIdAndStatus(companyId, status) :
            payrollRepository.findByCompanyId(companyId, pageable);

        return ResponseEntity.ok(PayrollDto.PageResponse.builder()
            .content(payrolls.getContent().stream().map(this::toPayrollResponse).collect(Collectors.toList()))
            .page(payrolls.getNumber())
            .size(payrolls.getSize())
            .totalElements(payrolls.getTotalElements())
            .totalPages(payrolls.getTotalPages())
            .first(payrolls.isFirst())
            .last(payrolls.isLast())
            .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get payroll details")
    public ResponseEntity<PayrollDto.PayrollResponse> get(@PathVariable Long id) {
        Payroll payroll = payrollRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found"));
        return ResponseEntity.ok(toPayrollResponse(payroll));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get payroll items")
    public ResponseEntity<List<PayrollDto.PayrollItemResponse>> getItems(
            @PathVariable Long id,
            @RequestParam(required = false) Long employeeId) {

        List<PayrollItem> items = employeeId != null ?
            payrollItemRepository.findByPayrollIdAndEmployeeId(id, employeeId) :
            payrollItemRepository.findByPayrollId(id);

        return ResponseEntity.ok(items.stream().map(this::toItemResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/items/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee payroll items")
    public ResponseEntity<PayrollDto.EmployeePayslipDetail> getEmployeePayslip(
            @PathVariable Long id,
            @PathVariable Long employeeId) {

        List<PayrollItem> items = payrollItemRepository.findByPayrollIdAndEmployeeIdOrdered(id, employeeId);
        Payslip payslip = payslipRepository.findByPayrollIdAndEmployeeId(id, employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        List<PayrollDto.PayrollItemResponse> earnings = items.stream()
            .filter(i -> i.getItemType() == PayrollItemType.EARNING)
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        List<PayrollDto.PayrollItemResponse> deductions = items.stream()
            .filter(i -> i.getItemType() == PayrollItemType.DEDUCTION)
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        List<PayrollDto.PayrollItemResponse> employerCosts = items.stream()
            .filter(i -> i.getItemType() == PayrollItemType.EMPLOYER_COST)
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(PayrollDto.EmployeePayslipDetail.builder()
            .payslip(toPayslipResponse(payslip))
            .earnings(earnings)
            .deductions(deductions)
            .employerCosts(employerCosts)
            .totalEarnings(earnings.stream().map(PayrollDto.PayrollItemResponse::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .totalDeductions(deductions.stream().map(PayrollDto.PayrollItemResponse::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .totalEmployerCosts(employerCosts.stream().map(PayrollDto.PayrollItemResponse::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .build());
    }

    @GetMapping("/payslips/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee payslips")
    public ResponseEntity<List<PayrollDto.PayslipResponse>> getEmployeePayslips(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("payroll.referenceYear").descending().and(Sort.by("payroll.referenceMonth").descending()));
        Page<Payslip> payslips = payslipRepository.findByEmployeeId(employeeId, pageable);

        return ResponseEntity.ok(payslips.getContent().stream().map(this::toPayslipResponse).collect(Collectors.toList()));
    }

    @GetMapping("/payslips/{payslipId}/download")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Download payslip PDF")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long payslipId) {
        // Would generate PDF and return
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get payroll summary for period")
    public ResponseEntity<PayrollDto.PayrollSummary> getSummary(
            @RequestParam Long companyId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        // Would aggregate data
        return ResponseEntity.ok(PayrollDto.PayrollSummary.builder()
            .totalEmployees(0)
            .totalGross(BigDecimal.ZERO)
            .totalNet(BigDecimal.ZERO)
            .totalInss(BigDecimal.ZERO)
            .totalIrrf(BigDecimal.ZERO)
            .totalFgts(BigDecimal.ZERO)
            .totalBenefits(BigDecimal.ZERO)
            .totalDeductions(BigDecimal.ZERO)
            .byCategory(Map.of())
            .build());
    }

    private PayrollDto.PayrollResponse toPayrollResponse(Payroll payroll) {
        return PayrollDto.PayrollResponse.builder()
            .id(payroll.getId())
            .uuid(payroll.getUuid())
            .companyId(payroll.getCompanyId())
            .referenceMonth(payroll.getReferenceMonth())
            .referenceYear(payroll.getReferenceYear())
            .referencePeriod(payroll.getReferencePeriod())
            .status(payroll.getStatus())
            .statusDescription(payroll.getStatus().getDescription())
            .totalEmployees(payroll.getTotalEmployees())
            .totalGrossAmount(payroll.getTotalGrossAmount())
            .totalNetAmount(payroll.getTotalNetAmount())
            .totalInssAmount(payroll.getTotalInssAmount())
            .totalIrrfAmount(payroll.getTotalIrrfAmount())
            .totalFgtsAmount(payroll.getTotalFgtsAmount())
            .totalBenefitsAmount(payroll.getTotalBenefitsAmount())
            .totalDeductionsAmount(payroll.getTotalDeductionsAmount())
            .calculatedAt(payroll.getCalculatedAt())
            .approvedAt(payroll.getApprovedAt())
            .processedAt(payroll.getProcessedAt())
            .paidAt(payroll.getPaidAt())
            .errorMessage(payroll.getErrorMessage())
            .observations(payroll.getObservations())
            .build();
    }

    private PayrollDto.PayrollItemResponse toItemResponse(PayrollItem item) {
        return PayrollDto.PayrollItemResponse.builder()
            .id(item.getId())
            .uuid(item.getUuid())
            .payrollId(item.getPayroll().getId())
            .employeeId(item.getEmployee().getId())
            .employeeName(item.getEmployee().getFullName())
            .itemType(item.getItemType())
            .itemTypeDescription(item.getItemType().getDescription())
            .category(item.getCategory())
            .categoryDescription(item.getCategory().getDescription())
            .code(item.getCode())
            .description(item.getDescription())
            .referenceValue(item.getReferenceValue())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalAmount(item.getTotalAmount())
            .taxableInss(item.getTaxableInss())
            .taxableIrrf(item.getTaxableIrrf())
            .taxableFgts(item.getTaxableFgts())
            .isFixed(item.getIsFixed())
            .calculationBasis(item.getCalculationBasis())
            .displayOrder(item.getDisplayOrder())
            .build();
    }

    private PayrollDto.PayslipResponse toPayslipResponse(Payslip payslip) {
        return PayrollDto.PayslipResponse.builder()
            .id(payslip.getId())
            .uuid(payslip.getUuid())
            .payrollId(payslip.getPayroll().getId())
            .employeeId(payslip.getEmployee().getId())
            .employeeName(payslip.getEmployee().getFullName())
            .employeeNumber(payslip.getEmployee().getEmployeeNumber())
            .grossAmount(payslip.getGrossAmount())
            .netAmount(payslip.getNetAmount())
            .inssBase(payslip.getInssBase())
            .inssAmount(payslip.getInssAmount())
            .irrfBase(payslip.getIrrfBase())
            .irrfAmount(payslip.getIrrfAmount())
            .fgtsBase(payslip.getFgtsBase())
            .fgtsAmount(payslip.getFgtsAmount())
            .fgtsDepositDate(payslip.getFgtsDepositDate())
            .pdfPath(payslip.getPdfPath())
            .pdfGeneratedAt(payslip.getPdfGeneratedAt())
            .digitalSignature(payslip.getDigitalSignature())
            .signedAt(payslip.getSignedAt())
            .deliveredAt(payslip.getDeliveredAt())
            .deliveryMethod(payslip.getDeliveryMethod() != null ? payslip.getDeliveryMethod().name() : null)
            .status(payslip.getStatus())
            .statusDescription(payslip.getStatus().getDescription())
            .accessToken(payslip.getAccessToken())
            .accessExpiresAt(payslip.getAccessExpiresAt())
            .downloadUrl(payslip.getAccessToken() != null ? "/api/hr/payroll/payslips/" + payslipId + "/download" : null)
            .build();
    }

    private Long getCurrentCompanyId() {
        return 1L;
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}