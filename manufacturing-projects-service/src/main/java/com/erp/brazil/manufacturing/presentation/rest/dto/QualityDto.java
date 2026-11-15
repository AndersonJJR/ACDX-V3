package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.QualityInspection;
import com.erp.brazil.manufacturing.domain.model.QualityInspectionResult;
import com.erp.brazil.manufacturing.domain.model.NonConformance;
import com.erp.brazil.manufacturing.domain.model.CorrectiveAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityInspectionDto {
        private UUID id;
        private String inspectionNumber;
        private String inspectionType;
        private String status;
        private UUID productId;
        private String lotNumber;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID workOrderId;
        private UUID goodsReceiptId;
        private UUID subcontractingReceiptId;
        private UUID samplingPlanId;
        private String inspectorId;
        private LocalDateTime inspectionDate;
        private Integer sampleSize;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String unitOfMeasure;
        private String disposition;
        private String notes;
        private List<QualityInspectionResultDto> results;
        private List<NonConformanceDto> nonConformances;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public static QualityInspectionDto fromEntity(QualityInspection qi) {
            return QualityInspectionDto.builder()
                    .id(qi.getId())
                    .inspectionNumber(qi.getInspectionNumber())
                    .inspectionType(qi.getInspectionType().name())
                    .status(qi.getStatus().name())
                    .productId(qi.getProductId())
                    .lotNumber(qi.getLotNumber())
                    .productionOrderId(qi.getProductionOrderId())
                    .productionOrderOperationId(qi.getProductionOrderOperationId())
                    .workOrderId(qi.getWorkOrderId())
                    .goodsReceiptId(qi.getGoodsReceiptId())
                    .subcontractingReceiptId(qi.getSubcontractingReceiptId())
                    .samplingPlanId(qi.getSamplingPlanId())
                    .inspectorId(qi.getInspectorId())
                    .inspectionDate(qi.getInspectionDate())
                    .sampleSize(qi.getSampleSize())
                    .acceptedQuantity(qi.getAcceptedQuantity())
                    .rejectedQuantity(qi.getRejectedQuantity())
                    .unitOfMeasure(qi.getUnitOfMeasure())
                    .disposition(qi.getDisposition() != null ? qi.getDisposition().name() : null)
                    .notes(qi.getNotes())
                    .results(qi.getResults().stream().map(QualityInspectionResultDto::fromEntity).toList())
                    .nonConformances(qi.getNonConformances().stream().map(NonConformanceDto::fromEntity).toList())
                    .createdAt(qi.getCreatedAt())
                    .updatedAt(qi.getUpdatedAt())
                    .createdBy(qi.getCreatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityInspectionResultDto {
        private UUID id;
        private UUID characteristicId;
        private String characteristicName;
        private BigDecimal specificationMin;
        private BigDecimal specificationMax;
        private BigDecimal specificationTarget;
        private String unitOfMeasure;
        private BigDecimal measuredValue;
        private String measuredValues;
        private String result;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static QualityInspectionResultDto fromEntity(QualityInspectionResult qir) {
            return QualityInspectionResultDto.builder()
                    .id(qir.getId())
                    .characteristicId(qir.getCharacteristicId())
                    .characteristicName(qir.getCharacteristicName())
                    .specificationMin(qir.getSpecificationMin())
                    .specificationMax(qir.getSpecificationMax())
                    .specificationTarget(qir.getSpecificationTarget())
                    .unitOfMeasure(qir.getUnitOfMeasure())
                    .measuredValue(qir.getMeasuredValue())
                    .measuredValues(qir.getMeasuredValues())
                    .result(qir.getResult() != null ? qir.getResult().name() : null)
                    .notes(qir.getNotes())
                    .createdAt(qir.getCreatedAt())
                    .updatedAt(qir.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NonConformanceDto {
        private UUID id;
        private String ncNumber;
        private UUID qualityInspectionId;
        private UUID productId;
        private String lotNumber;
        private UUID productionOrderId;
        private UUID workOrderId;
        private String severity;
        private String status;
        private String source;
        private String description;
        private String rootCause;
        private String identifiedBy;
        private LocalDateTime identifiedDate;
        private String assignedTo;
        private LocalDateTime targetResolutionDate;
        private LocalDateTime actualResolutionDate;
        private BigDecimal quantityAffected;
        private String unitOfMeasure;
        private BigDecimal costImpact;
        private String currency;
        private String notes;
        private List<CorrectiveActionDto> correctiveActions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public static NonConformanceDto fromEntity(NonConformance nc) {
            return NonConformanceDto.builder()
                    .id(nc.getId())
                    .ncNumber(nc.getNcNumber())
                    .qualityInspectionId(nc.getQualityInspection() != null ? nc.getQualityInspection().getId() : null)
                    .productId(nc.getProductId())
                    .lotNumber(nc.getLotNumber())
                    .productionOrderId(nc.getProductionOrderId())
                    .workOrderId(nc.getWorkOrderId())
                    .severity(nc.getSeverity().name())
                    .status(nc.getStatus().name())
                    .source(nc.getSource() != null ? nc.getSource().name() : null)
                    .description(nc.getDescription())
                    .rootCause(nc.getRootCause())
                    .identifiedBy(nc.getIdentifiedBy())
                    .identifiedDate(nc.getIdentifiedDate())
                    .assignedTo(nc.getAssignedTo())
                    .targetResolutionDate(nc.getTargetResolutionDate())
                    .actualResolutionDate(nc.getActualResolutionDate())
                    .quantityAffected(nc.getQuantityAffected())
                    .unitOfMeasure(nc.getUnitOfMeasure())
                    .costImpact(nc.getCostImpact())
                    .currency(nc.getCurrency())
                    .notes(nc.getNotes())
                    .correctiveActions(nc.getCorrectiveActions().stream().map(CorrectiveActionDto::fromEntity).toList())
                    .createdAt(nc.getCreatedAt())
                    .updatedAt(nc.getUpdatedAt())
                    .createdBy(nc.getCreatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectiveActionDto {
        private UUID id;
        private UUID nonConformanceId;
        private String actionNumber;
        private String type;
        private String status;
        private String description;
        private String rootCauseAnalysis;
        private String assignedTo;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedCompletionDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualCompletionDate;
        private String verificationMethod;
        private String verificationResult;
        private String verifiedBy;
        private LocalDateTime verifiedAt;
        private LocalDateTime effectivenessCheckDate;
        private String effectivenessResult;
        private BigDecimal cost;
        private String currency;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public static CorrectiveActionDto fromEntity(CorrectiveAction ca) {
            return CorrectiveActionDto.builder()
                    .id(ca.getId())
                    .nonConformanceId(ca.getNonConformance().getId())
                    .actionNumber(ca.getActionNumber())
                    .type(ca.getType().name())
                    .status(ca.getStatus().name())
                    .description(ca.getDescription())
                    .rootCauseAnalysis(ca.getRootCauseAnalysis())
                    .assignedTo(ca.getAssignedTo())
                    .plannedStartDate(ca.getPlannedStartDate())
                    .plannedCompletionDate(ca.getPlannedCompletionDate())
                    .actualStartDate(ca.getActualStartDate())
                    .actualCompletionDate(ca.getActualCompletionDate())
                    .verificationMethod(ca.getVerificationMethod())
                    .verificationResult(ca.getVerificationResult())
                    .verifiedBy(ca.getVerifiedBy())
                    .verifiedAt(ca.getVerifiedAt())
                    .effectivenessCheckDate(ca.getEffectivenessCheckDate())
                    .effectivenessResult(ca.getEffectivenessResult())
                    .cost(ca.getCost())
                    .currency(ca.getCurrency())
                    .notes(ca.getNotes())
                    .createdAt(ca.getCreatedAt())
                    .updatedAt(ca.getUpdatedAt())
                    .createdBy(ca.getCreatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectQualityRequest {
        private Integer sampleSize;
        private List<InspectionResultRequest> results;
        private String notes;
        private String inspectedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionResultRequest {
        private UUID characteristicId;
        private String characteristicName;
        private BigDecimal specificationMin;
        private BigDecimal specificationMax;
        private BigDecimal specificationTarget;
        private String unitOfMeasure;
        private BigDecimal measuredValue;
        private String measuredValuesJson;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCorrectiveActionRequest {
        private UUID nonConformanceId;
        private String actionNumber;
        private String type;
        private String description;
        private String rootCauseAnalysis;
        private String assignedTo;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedCompletionDate;
        private String verificationMethod;
        private BigDecimal cost;
        private String currency;
        private String notes;
        private String createdBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyCorrectiveActionRequest {
        private String verificationResult;
        private String verifiedBy;
        private Boolean effectivenessVerified;
        private String effectivenessResult;
    }
}