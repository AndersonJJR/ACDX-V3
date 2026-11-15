package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BOMItemDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private BigDecimal scrapPercentage;
        private Integer position;
        private String unitOfMeasure;
        private Boolean isPhantom;
        private Boolean isOptional;
        private String referenceDesignator;
        private String findNumber;
        private String notes;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID bomId;
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private BigDecimal scrapPercentage;
        private Integer position;
        private String unitOfMeasure;
        private Boolean isPhantom;
        private Boolean isOptional;
        private String referenceDesignator;
        private String findNumber;
        private String notes;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}