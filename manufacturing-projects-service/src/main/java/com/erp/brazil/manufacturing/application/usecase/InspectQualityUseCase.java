package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.QualityInspectionFailedEvent;
import com.erp.brazil.manufacturing.domain.event.QualityInspectionPassedEvent;
import com.erp.brazil.manufacturing.domain.model.NonConformance;
import com.erp.brazil.manufacturing.domain.model.QualityInspection;
import com.erp.brazil.manufacturing.domain.model.QualityInspectionResult;
import com.erp.brazil.manufacturing.domain.repository.NonConformanceRepository;
import com.erp.brazil.manufacturing.domain.repository.QualityInspectionRepository;
import com.erp.brazil.manufacturing.domain.repository.QualityInspectionResultRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InspectQualityUseCase {

    private final QualityInspectionRepository inspectionRepository;
    private final QualityInspectionResultRepository resultRepository;
    private final NonConformanceRepository ncRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InspectQualityUseCase(QualityInspectionRepository inspectionRepository,
                                  QualityInspectionResultRepository resultRepository,
                                  NonConformanceRepository ncRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.inspectionRepository = inspectionRepository;
        this.resultRepository = resultRepository;
        this.ncRepository = ncRepository;
        this.eventPublisher = eventPublisher;
    }

    public QualityInspection createInspection(QualityInspection.InspectionType type,
                                               UUID productId,
                                               String lotNumber,
                                               UUID productionOrderId,
                                               UUID workOrderId,
                                               UUID goodsReceiptId,
                                               UUID subcontractingReceiptId,
                                               UUID samplingPlanId,
                                               Integer sampleSize,
                                               String inspectorId,
                                               LocalDateTime inspectionDate) {
        String inspectionNumber = generateInspectionNumber();

        QualityInspection inspection = QualityInspection.builder()
            .inspectionNumber(inspectionNumber)
            .inspectionType(type)
            .status(QualityInspection.InspectionStatus.PENDING)
            .productId(productId)
            .lotNumber(lotNumber)
            .productionOrderId(productionOrderId)
            .workOrderId(workOrderId)
            .goodsReceiptId(goodsReceiptId)
            .subcontractingReceiptId(subcontractingReceiptId)
            .samplingPlanId(samplingPlanId)
            .inspectorId(inspectorId)
            .inspectionDate(inspectionDate)
            .sampleSize(sampleSize)
            .build();

        return inspectionRepository.save(inspection);
    }

    public QualityInspection startInspection(UUID inspectionId, String startedBy) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + inspectionId));

        if (inspection.getStatus() != QualityInspection.InspectionStatus.PENDING) {
            throw new IllegalStateException("Inspection cannot be started. Current status: " + inspection.getStatus());
        }

        inspection.setStatus(QualityInspection.InspectionStatus.IN_PROGRESS);
        return inspectionRepository.save(inspection);
    }

    public void recordResult(UUID inspectionId, UUID characteristicId,
                              BigDecimal measuredValue,
                              BigDecimal specMin, BigDecimal specMax, BigDecimal specTarget,
                              String unitOfMeasure,
                              String notes) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + inspectionId));

        QualityInspectionResult.Result result;
        if (measuredValue.compareTo(specMin) < 0 || measuredValue.compareTo(specMax) > 0) {
            result = QualityInspectionResult.Result.FAIL;
        } else if (measuredValue.compareTo(specMin.multiply(BigDecimal.valueOf(1.1))) < 0 ||
                   measuredValue.compareTo(specMax.multiply(BigDecimal.valueOf(0.9))) > 0) {
            result = QualityInspectionResult.Result.MARGINAL;
        } else {
            result = QualityInspectionResult.Result.PASS;
        }

        QualityInspectionResult resultEntity = QualityInspectionResult.builder()
            .qualityInspection(inspection)
            .characteristicId(characteristicId)
            .specificationMin(specMin)
            .specificationMax(specMax)
            .specificationTarget(specTarget)
            .unitOfMeasure(unitOfMeasure)
            .measuredValue(measuredValue)
            .result(result)
            .notes(notes)
            .build();

        resultRepository.save(resultEntity);
        inspection.addResult(resultEntity);
        inspectionRepository.save(inspection);
    }

    public QualityInspection completeInspection(UUID inspectionId,
                                                 BigDecimal acceptedQuantity,
                                                 BigDecimal rejectedQuantity,
                                                 String disposition,
                                                 String notes,
                                                 String completedBy) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + inspectionId));

        inspection.setStatus(QualityInspection.InspectionStatus.COMPLETED);
        inspection.setAcceptedQuantity(acceptedQuantity);
        inspection.setRejectedQuantity(rejectedQuantity);
        inspection.setDisposition(QualityInspection.Disposition.valueOf(disposition.toUpperCase()));
        inspection.setNotes(notes);

        // Determine pass/fail
        boolean hasFailures = inspection.getResults().stream()
            .anyMatch(r -> r.getResult() == QualityInspectionResult.Result.FAIL);

        if (hasFailures || rejectedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            inspection.setStatus(QualityInspection.InspectionStatus.FAILED);
        } else {
            inspection.setStatus(QualityInspection.InspectionStatus.PASSED);
        }

        QualityInspection saved = inspectionRepository.save(inspection);

        // Create non-conformance if failed
        if (saved.getStatus() == QualityInspection.InspectionStatus.FAILED) {
            createNonConformance(saved, completedBy);
        }

        // Publish events
        if (saved.getStatus() == QualityInspection.InspectionStatus.PASSED) {
            eventPublisher.publishEvent(QualityInspectionPassedEvent.builder()
                .qualityInspectionId(saved.getId())
                .inspectionNumber(saved.getInspectionNumber())
                .productId(saved.getProductId())
                .lotNumber(saved.getLotNumber())
                .acceptedQuantity(saved.getAcceptedQuantity())
                .warehouseId(null) // Would come from context
                .inspectedBy(completedBy)
                .build());
        } else if (saved.getStatus() == QualityInspection.InspectionStatus.FAILED) {
            // Get the NC number created
            NonConformance nc = ncRepository.findByQualityInspectionId(saved.getId())
                .stream().findFirst().orElse(null);

            eventPublisher.publishEvent(QualityInspectionFailedEvent.builder()
                .qualityInspectionId(saved.getId())
                .inspectionNumber(saved.getInspectionNumber())
                .productId(saved.getProductId())
                .lotNumber(saved.getLotNumber())
                .rejectedQuantity(saved.getRejectedQuantity())
                .warehouseId(null)
                .inspectedBy(completedBy)
                .nonConformanceNumber(nc != null ? nc.getNcNumber() : null)
                .build());
        }

        return saved;
    }

    private void createNonConformance(QualityInspection inspection, String identifiedBy) {
        NonConformance nc = NonConformance.builder()
            .ncNumber(generateNCNumber())
            .qualityInspection(inspection)
            .productId(inspection.getProductId())
            .lotNumber(inspection.getLotNumber())
            .productionOrderId(inspection.getProductionOrderId())
            .workOrderId(inspection.getWorkOrderId())
            .severity(determineSeverity(inspection))
            .status(NonConformance.NCStatus.OPEN)
            .source(NonConformance.Source.INSPECTION)
            .description("Quality inspection failed: " + inspection.getInspectionNumber())
            .identifiedBy(identifiedBy)
            .identifiedDate(LocalDateTime.now())
            .quantityAffected(inspection.getRejectedQuantity())
            .unitOfMeasure(inspection.getUnitOfMeasure())
            .build();

        ncRepository.save(nc);
        inspection.addNonConformance(nc);
    }

    private NonConformance.Severity determineSeverity(QualityInspection inspection) {
        long criticalFailures = inspection.getResults().stream()
            .filter(r -> r.getResult() == QualityInspectionResult.Result.FAIL)
            .count();

        if (criticalFailures > 3) {
            return NonConformance.Severity.CRITICAL;
        } else if (criticalFailures > 1) {
            return NonConformance.Severity.MAJOR;
        } else {
            return NonConformance.Severity.MINOR;
        }
    }

    private String generateInspectionNumber() {
        long count = inspectionRepository.count() + 1;
        return String.format("QI-%06d", count);
    }

    private String generateNCNumber() {
        long count = ncRepository.count() + 1;
        return String.format("NC-%06d", count);
    }
}