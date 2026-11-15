package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BOMOperationDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Integer sequence;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private String description;
        private BigDecimal setupTimeMinutes;
        private BigDecimal runTimeMinutes;
        private BigDecimal queueTimeMinutes;
        private BigDecimal moveTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID bomId;
        private Integer sequence;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private String description;
        private BigDecimal setupTimeMinutes;
        private BigDecimal runTimeMinutes;
        private BigDecimal queueTimeMinutes;
        private BigDecimal moveTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}