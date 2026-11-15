package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductionOrderOperationDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Integer sequence;
        private UUID routingOperationId;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private String description;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private Boolean inspectionRequired;
        private UUID samplingPlanId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID productionOrderId;
        private Integer sequence;
        private UUID routingOperationId;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private String description;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private UUID subcontractingOrderId;
        private String status;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private BigDecimal completedQuantity;
        private BigDecimal scrappedQuantity;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}