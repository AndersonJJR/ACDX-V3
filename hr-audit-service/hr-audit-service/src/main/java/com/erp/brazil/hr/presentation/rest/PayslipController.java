package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.PayslipDTO;
import com.erp.brazil.hr.application.usecase.GeneratePayslipUseCase;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.Payslip;
import com.erp.brazil.hr.domain.repository.PayslipRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payslips")
@Tag(name = "Payslips", description = "Payslip (Holerite) management API")
public class PayslipController {

    private final PayslipRepository payslipRepository;
    private final GeneratePayslipUseCase generatePayslipUseCase;
    private final ProcessWebhookUseCase webhookUseCase;

    public PayslipController(PayslipRepository payslipRepository,
                              GeneratePayslipUseCase generatePayslipUseCase,
                              ProcessWebhookUseCase webhookUseCase) {
        this.payslipRepository = payslipRepository;
        this.generatePayslipUseCase = generatePayslipUseCase;
        this.webhookUseCase = webhookUseCase;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payslip by ID")
    public ResponseEntity<PayslipDTO> getById(@PathVariable UUID id) {
        return payslipRepository.findByUuid(id)
            .map(PayslipDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download payslip PDF")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Payslip payslip = payslipRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + id));

        if (payslip.pdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(payslip.pdfPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"holerite_" + id + ".pdf\"")
                .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{employeeId}/list")
    @Operation(summary = "List payslips for employee")
    public ResponseEntity<Page<PayslipDTO>> listByEmployee(
            @PathVariable Long employeeId,
            @Parameter(description = "Reference year") @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PayslipDTO> page;
        if (year != null) {
            page = payslipRepository.findByEmployeeIdAndYear(employeeId, year, pageable)
                .map(PayslipDTO::from);
        } else {
            page = payslipRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable)
                .map(PayslipDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/payroll/{payrollId}")
    @Operation(summary = "List all payslips for a payroll period")
    public ResponseEntity<List<PayslipDTO>> listByPayroll(@PathVariable UUID payrollId) {
        // Would need to find payroll by UUID first
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "Digitally sign a payslip")
    public ResponseEntity<PayslipDTO> sign(@PathVariable UUID id, @RequestParam String digitalSignature) {
        Payslip signed = generatePayslipUseCase.sign(id, digitalSignature);
        webhookUseCase.triggerEvent("PAYSLIP_SIGNED", Map.of("payslip", signed), null);
        return ResponseEntity.ok(PayslipDTO.from(signed));
    }

    @GetMapping("/{id}/access/{token}")
    @Operation(summary = "Access payslip via secure token")
    public ResponseEntity<PayslipDTO> accessByToken(@PathVariable UUID id, @PathVariable String token) {
        Payslip payslip = payslipRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payslip not found: " + id));

        if (!payslip.accessToken().equals(token) || !payslip.isAccessible()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(PayslipDTO.from(payslip));
    }
}