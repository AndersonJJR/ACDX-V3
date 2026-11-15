package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WorkOrderDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID workCenterId;
        private UUID workStationId;
        private String priority;
        private String operationName;
        private String description;
        private BigDecimal plannedQuantity;
        private String unitOfMeasure;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private String assignedTo;
        private String notes;
        private List<WorkOrderOperationDTO.Request> operations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String orderNumber;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID workCenterId;
        private UUID workStationId;
        private String status;
        private String priority;
        private String operationName;
        private String description;
        private BigDecimal plannedQuantity;
        private BigDecimal startedQuantity;
        private BigDecimal completedQuantity;
        private BigDecimal scrappedQuantity;
        private String unitOfMeasure;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private String assignedTo;
        private String notes;
        private List<WorkOrderOperationDTO.Response> operations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartRequest {
        private String startedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompleteRequest {
        private BigDecimal completedQuantity;
        private BigDecimal scrappedQuantity;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private String completedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportProductionRequest {
        private BigDecimal quantityProduced;
        private BigDecimal quantityScrapped;
        private String reportedBy;
    }
}