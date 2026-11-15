package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class QualityDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InspectionRequest {
        private String inspectionType;
        private UUID productId;
        private String lotNumber;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID workOrderId;
        private UUID goodsReceiptId;
        private UUID subcontractingReceiptId;
        private UUID samplingPlanId;
        private Integer sampleSize;
        private String inspectorId;
        private LocalDateTime inspectionDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InspectionResponse {
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
        private List<InspectionResultDTO.Response> results;
        private List<NonConformanceDTO.Response> nonConformances;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultRequest {
        private UUID characteristicId;
        private BigDecimal measuredValue;
        private BigDecimal specMin;
        private BigDecimal specMax;
        private BigDecimal specTarget;
        private String unitOfMeasure;
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompleteInspectionRequest {
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String disposition;
        private String notes;
        private String completedBy;
    }

    public static class InspectionResultDTO {
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private UUID id;
            private UUID qualityInspectionId;
            private UUID characteristicId;
            private String characteristicName;
            private BigDecimal specificationMin;
            private BigDecimal specificationMax;
            private BigDecimal specificationTarget;
            private String unitOfMeasure;
            private BigDecimal measuredValue;
            private String result;
            private String notes;
            private LocalDateTime createdAt;
            private LocalDateTime updatedAt;
        }
    }

    public static class NonConformanceDTO {
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private UUID id;
            private String ncNumber;
            private UUID qualityInspectionId;
            private UUID productId;
            private String lotNumber;
            private UUID productionOrderId;
            private UUID workOrderId.
            private String severity.
            private String status.
            private String source.
            private String description.
            private String rootCause.
            private String identifiedBy.
            private LocalDateTime identifiedDate.
            private String assignedTo.
            private LocalDateTime targetResolutionDate.
            private LocalDateTime actualResolutionDate.
            private BigDecimal quantityAffected.
            private String unitOfMeasure.
            private BigDecimal costImpact.
            private String currency.
            private String notes.
            private List<CorrectiveActionDTO.Response> correctiveActions.
            private LocalDateTime createdAt.
            private LocalDateTime updatedAt.
        }
    }

    public static class CorrectiveActionDTO {
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Response {
            private UUID id.
            private UUID nonConformanceId.
            private String actionNumber.
            private String type.
            private String status.
            private String description.
            private String rootCauseAnalysis.
            private String assignedTo.
            private LocalDateTime plannedStartDate.
            private LocalDateTime plannedCompletionDate.
            private LocalDateTime actualStartDate.
            private LocalDateTime actualCompletionDate.
            private String verificationMethod.
            private String verificationResult.
            private String verifiedBy.
            private LocalDateTime verifiedAt.
            private LocalDateTime effectivenessCheckDate.
            private String effectivenessResult.
            private BigDecimal cost.
            private String currency.
            private String notes.
            private LocalDateTime createdAt.
            private LocalDateTime updatedAt.
        }
    }
}