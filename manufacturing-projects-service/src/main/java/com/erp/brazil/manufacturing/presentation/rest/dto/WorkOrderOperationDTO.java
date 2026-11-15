package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WorkOrderOperationDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Integer sequence;
        private String operationName;
        private String description;
        private UUID workCenterId;
        private UUID workStationId;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID workOrderId;
        private Integer sequence;
        private String operationName;
        private String description;
        private UUID workCenterId;
        private UUID workStationId;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private String status;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private String assignedTo;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}