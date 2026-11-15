package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.InspectQualityUseCase;
import com.erp.brazil.manufacturing.domain.model.QualityInspection;
import com.erp.brazil.manufacturing.domain.model.QualityInspection.InspectionStatus;
import com.erp.brazil.manufacturing.domain.model.QualityInspection.InspectionType;
import com.erp.brazil.manufacturing.domain.model.NonConformance;
import com.erp.brazil.manufacturing.domain.model.NonConformance.NCStatus;
import com.erp.brazil.manufacturing.domain.model.CorrectiveAction;
import com.erp.brazil.manufacturing.domain.repository.QualityInspectionRepository;
import com.erp.brazil.manufacturing.domain.repository.NonConformanceRepository;
import com.erp.brazil.manufacturing.domain.repository.CorrectiveActionRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.QualityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quality")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quality", description = "Quality inspection management")
public class QualityController {

    private final QualityInspectionRepository qualityInspectionRepository;
    private final NonConformanceRepository nonConformanceRepository;
    private final CorrectiveActionRepository correctiveActionRepository;
    private final InspectQualityUseCase inspectQualityUseCase;

    @PostMapping("/inspect")
    @Operation(summary = "Perform quality inspection")
    public ResponseEntity<QualityDto.QualityInspectionDto> inspectQuality(@Valid @RequestBody QualityDto.InspectQualityRequest request) {
        log.info("Performing quality inspection {}", request.getInspectionId());

        var command = InspectQualityUseCase.InspectQualityCommand.builder()
                .inspectionId(request.getInspectionId())
                .sampleSize(request.getSampleSize())
                .results(request.getResults().stream().map(r ->
                        InspectQualityUseCase.InspectionResultCommand.builder()
                                .characteristicId(r.getCharacteristicId())
                                .characteristicName(r.getCharacteristicName())
                                .specificationMin(r.getSpecificationMin())
                                .specificationMax(r.getSpecificationMax())
                                .specificationTarget(r.getSpecificationTarget())
                                .unitOfMeasure(r.getUnitOfMeasure())
                                .measuredValue(r.getMeasuredValue())
                                .measuredValuesJson(r.getMeasuredValuesJson())
                                .notes(r.getNotes())
                                .build()
                ).toList())
                .notes(request.getNotes())
                .inspectedBy(request.getInspectedBy())
                .build();

        var result = inspectQualityUseCase.execute(command);
        QualityInspection qi = qualityInspectionRepository.findById(result.inspectionId()).orElseThrow();
        return ResponseEntity.ok(QualityDto.QualityInspectionDto.fromEntity(qi));
    }

    @GetMapping("/inspections/{id}")
    @Operation(summary = "Get quality inspection by ID")
    public ResponseEntity<QualityDto.QualityInspectionDto> getQualityInspection(@PathVariable UUID id) {
        QualityInspection qi = qualityInspectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quality inspection not found: " + id));
        return ResponseEntity.ok(QualityDto.QualityInspectionDto.fromEntity(qi));
    }

    @GetMapping("/inspections")
    @Operation(summary = "List quality inspections with pagination")
    public ResponseEntity<Page<QualityDto.QualityInspectionDto>> listQualityInspections(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID productionOrderId,
            @RequestParam(required = false) UUID workOrderId,
            @RequestParam(required = false) String inspectorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QualityInspection> inspections;
        if (type != null && status != null) {
            inspections = qualityInspectionRepository.findByInspectionTypeAndStatus(
                    InspectionType.valueOf(type), InspectionStatus.valueOf(status), pageable);
        } else if (type != null) {
            inspections = qualityInspectionRepository.findByInspectionType(InspectionType.valueOf(type), pageable);
        } else if (status != null) {
            inspections = qualityInspectionRepository.findByStatus(InspectionStatus.valueOf(status), pageable);
        } else if (productId != null) {
            inspections = qualityInspectionRepository.findByProductId(productId, pageable);
        } else if (productionOrderId != null) {
            inspections = qualityInspectionRepository.findByProductionOrderId(productionOrderId, pageable);
        } else if (workOrderId != null) {
            inspections = qualityInspectionRepository.findByWorkOrderId(workOrderId, pageable);
        } else if (inspectorId != null) {
            inspections = qualityInspectionRepository.findAssignedToInspector(inspectorId, pageable);
        } else {
            inspections = qualityInspectionRepository.findAll(pageable);
        }
        return ResponseEntity.ok(inspections.map(QualityDto.QualityInspectionDto::fromEntity));
    }

    @PostMapping("/inspections")
    @Operation(summary = "Create quality inspection")
    public ResponseEntity<QualityDto.QualityInspectionDto> createQualityInspection(
            @Valid @RequestBody QualityDto.QualityInspectionDto inspection) {
        QualityInspection qi = QualityInspection.builder()
                .inspectionNumber(inspection.getInspectionNumber())
                .inspectionType(InspectionType.valueOf(inspection.getInspectionType()))
                .status(InspectionStatus.PENDING)
                .productId(inspection.getProductId())
                .lotNumber(inspection.getLotNumber())
                .productionOrderId(inspection.getProductionOrderId())
                .productionOrderOperationId(inspection.getProductionOrderOperationId())
                .workOrderId(inspection.getWorkOrderId())
                .goodsReceiptId(inspection.getGoodsReceiptId())
                .subcontractingReceiptId(inspection.getSubcontractingReceiptId())
                .samplingPlanId(inspection.getSamplingPlanId())
                .inspectorId(inspection.getInspectorId())
                .inspectionDate(inspection.getInspectionDate() != null ? inspection.getInspectionDate() : LocalDateTime.now())
                .sampleSize(inspection.getSampleSize())
                .unitOfMeasure(inspection.getUnitOfMeasure())
                .notes(inspection.getNotes())
                .createdBy(inspection.getCreatedBy())
                .build();

        qualityInspectionRepository.save(qi);
        return ResponseEntity.status(HttpStatus.CREATED).body(QualityDto.QualityInspectionDto.fromEntity(qi));
    }

    @GetMapping("/inspections/{id}/non-conformances")
    @Operation(summary = "Get non-conformances for inspection")
    public ResponseEntity<List<QualityDto.NonConformanceDto>> getNonConformancesForInspection(@PathVariable UUID id) {
        QualityInspection qi = qualityInspectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quality inspection not found: " + id));
        return ResponseEntity.ok(qi.getNonConformances().stream().map(QualityDto.NonConformanceDto::fromEntity).toList());
    }

    @GetMapping("/non-conformances/{id}")
    @Operation(summary = "Get non-conformance by ID")
    public ResponseEntity<QualityDto.NonConformanceDto> getNonConformance(@PathVariable UUID id) {
        NonConformance nc = nonConformanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Non-conformance not found: " + id));
        return ResponseEntity.ok(QualityDto.NonConformanceDto.fromEntity(nc));
    }

    @GetMapping("/non-conformances")
    @Operation(summary = "List non-conformances with pagination")
    public ResponseEntity<Page<QualityDto.NonConformanceDto>> listNonConformances(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String assignedTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NonConformance> ncs;
        if (status != null) {
            ncs = nonConformanceRepository.findByStatus(NCStatus.valueOf(status), pageable);
        } else if (severity != null) {
            ncs = nonConformanceRepository.findBySeverity(com.erp.brazil.manufacturing.domain.model.NonConformance.Severity.valueOf(severity), pageable);
        } else if (productId != null) {
            ncs = nonConformanceRepository.findByProductId(productId, pageable);
        } else if (assignedTo != null) {
            ncs = nonConformanceRepository.findOpenByAssignee(assignedTo, pageable);
        } else {
            ncs = nonConformanceRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ncs.map(QualityDto.NonConformanceDto::fromEntity));
    }

    @PostMapping("/non-conformances/{id}/corrective-action")
    @Operation(summary = "Create corrective action for non-conformance")
    public ResponseEntity<QualityDto.CorrectiveActionDto> createCorrectiveAction(
            @PathVariable UUID id,
            @Valid @RequestBody QualityDto.CreateCorrectiveActionRequest request) {
        NonConformance nc = nonConformanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Non-conformance not found: " + id));

        CorrectiveAction ca = CorrectiveAction.builder()
                .nonConformance(nc)
                .actionNumber(request.getActionNumber())
                .type(com.erp.brazil.manufacturing.domain.model.CorrectiveAction.ActionType.valueOf(request.getType()))
                .description(request.getDescription())
                .rootCauseAnalysis(request.getRootCauseAnalysis())
                .assignedTo(request.getAssignedTo())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedCompletionDate(request.getPlannedCompletionDate())
                .verificationMethod(request.getVerificationMethod())
                .cost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .build();

        correctiveActionRepository.save(ca);
        nc.addCorrectiveAction(ca);
        nonConformanceRepository.save(nc);

        return ResponseEntity.status(HttpStatus.CREATED).body(QualityDto.CorrectiveActionDto.fromEntity(ca));
    }

    @PatchMapping("/non-conformances/{id}/verify")
    @Operation(summary = "Verify corrective action")
    public ResponseEntity<QualityDto.CorrectiveActionDto> verifyCorrectiveAction(
            @PathVariable UUID id,
            @Valid @RequestBody QualityDto.VerifyCorrectiveActionRequest request) {
        CorrectiveAction ca = correctiveActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corrective action not found: " + id));

        ca.setVerificationResult(request.getVerificationResult());
        ca.setVerifiedBy(request.getVerifiedBy());
        ca.setVerifiedAt(LocalDateTime.now());
        ca.setStatus(com.erp.brazil.manufacturing.domain.model.CorrectiveAction.ActionStatus.VERIFIED);

        if (Boolean.TRUE.equals(request.getEffectivenessVerified())) {
            ca.setEffectivenessCheckDate(LocalDateTime.now());
            ca.setEffectivenessResult(request.getEffectivenessResult());
            ca.setStatus(com.erp.brazil.manufacturing.domain.model.CorrectiveAction.ActionStatus.EFFECTIVE);
        }

        correctiveActionRepository.save(ca);
        return ResponseEntity.ok(QualityDto.CorrectiveActionDto.fromEntity(ca));
    }

    @GetMapping("/non-conformances/{id}/corrective-actions")
    @Operation(summary = "Get corrective actions for non-conformance")
    public ResponseEntity<List<QualityDto.CorrectiveActionDto>> getCorrectiveActions(@PathVariable UUID id) {
        NonConformance nc = nonConformanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Non-conformance not found: " + id));
        return ResponseEntity.ok(nc.getCorrectiveActions().stream().map(QualityDto.CorrectiveActionDto::fromEntity).toList());
    }

    @GetMapping("/non-conformances/overdue")
    @Operation(summary = "Get overdue non-conformances")
    public ResponseEntity<List<QualityDto.NonConformanceDto>> getOverdueNonConformances() {
        List<NonConformance> ncs = nonConformanceRepository.findOverdue(LocalDateTime.now());
        return ResponseEntity.ok(ncs.stream().map(QualityDto.NonConformanceDto::fromEntity).toList());
    }

    @GetMapping("/corrective-actions/overdue")
    @Operation(summary = "Get overdue corrective actions")
    public ResponseEntity<List<QualityDto.CorrectiveActionDto>> getOverdueCorrectiveActions() {
        List<CorrectiveAction> cas = correctiveActionRepository.findOverdue(LocalDateTime.now());
        return ResponseEntity.ok(cas.stream().map(QualityDto.CorrectiveActionDto::fromEntity).toList());
    }

    @GetMapping("/corrective-actions/due-effectiveness-check")
    @Operation(summary = "Get corrective actions due for effectiveness check")
    public ResponseEntity<List<QualityDto.CorrectiveActionDto>> getDueForEffectivenessCheck() {
        List<CorrectiveAction> cas = correctiveActionRepository.findDueForEffectivenessCheck(LocalDateTime.now());
        return ResponseEntity.ok(cas.stream().map(QualityDto.CorrectiveActionDto::fromEntity).toList());
    }
}