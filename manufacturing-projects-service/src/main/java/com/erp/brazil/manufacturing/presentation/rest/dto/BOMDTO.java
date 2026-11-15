package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BOMDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID productId;
        private String type;
        private String version;
        private String description;
        private BigDecimal baseQuantity;
        private BigDecimal scrapPercentage;
        private Boolean isPhantom;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
        private List<BOMItemDTO.Request> items;
        private List<BOMOperationDTO.Request> operations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID productId;
        private String type;
        private String status;
        private String version;
        private String description;
        private BigDecimal baseQuantity;
        private BigDecimal scrapPercentage;
        private Boolean isPhantom;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
        private List<BOMItemDTO.Response> items;
        private List<BOMOperationDTO.Response> operations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplosionRequest {
        private UUID bomId;
        private BigDecimal quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplosionResponse {
        private UUID bomId;
        private UUID productId;
        private String bomType;
        private BigDecimal baseQuantity;
        private BigDecimal totalQuantity;
        private BigDecimal scrapPercentage;
        private List<ExplosionItem> items;
        private List<ExplosionOperation> operations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplosionItem {
        private UUID bomItemId;
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private String unitOfMeasure;
        private Integer position;
        private Boolean isPhantom;
        private int level;
        private List<ExplosionItem> children;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExplosionOperation {
        private UUID operationId;
        private Integer sequence;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private BigDecimal setupTimeMinutes;
        private BigDecimal runTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private int level;
    }
}