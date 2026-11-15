package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.BOMItem;
import com.erp.brazil.manufacturing.domain.model.BOMOperation;
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
public class BOMDto {

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
    private List<BOMItemDto> items;
    private List<BOMOperationDto> operations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static BOMDto fromEntity(BOM bom) {
        return BOMDto.builder()
                .id(bom.getId())
                .productId(bom.getProductId())
                .type(bom.getType().name())
                .status(bom.getStatus().name())
                .version(bom.getVersion())
                .description(bom.getDescription())
                .baseQuantity(bom.getBaseQuantity())
                .scrapPercentage(bom.getScrapPercentage())
                .isPhantom(bom.getIsPhantom())
                .effectiveFrom(bom.getEffectiveFrom())
                .effectiveTo(bom.getEffectiveTo())
                .items(bom.getItems().stream().map(BOMItemDto::fromEntity).toList())
                .operations(bom.getOperations().stream().map(BOMOperationDto::fromEntity).toList())
                .createdAt(bom.getCreatedAt())
                .updatedAt(bom.getUpdatedAt())
                .createdBy(bom.getCreatedBy())
                .updatedBy(bom.getUpdatedBy())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMItemDto {
        private UUID id;
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

        public static BOMItemDto fromEntity(BOMItem item) {
            return BOMItemDto.builder()
                    .id(item.getId())
                    .componentId(item.getComponentId())
                    .componentType(item.getComponentType())
                    .quantity(item.getQuantity())
                    .scrapPercentage(item.getScrapPercentage())
                    .position(item.getPosition())
                    .unitOfMeasure(item.getUnitOfMeasure())
                    .isPhantom(item.getIsPhantom())
                    .isOptional(item.getIsOptional())
                    .referenceDesignator(item.getReferenceDesignator())
                    .findNumber(item.getFindNumber())
                    .notes(item.getNotes())
                    .effectiveFrom(item.getEffectiveFrom())
                    .effectiveTo(item.getEffectiveTo())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMOperationDto {
        private UUID id;
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

        public static BOMOperationDto fromEntity(BOMOperation op) {
            return BOMOperationDto.builder()
                    .id(op.getId())
                    .sequence(op.getSequence())
                    .workCenterId(op.getWorkCenterId())
                    .workStationId(op.getWorkStationId())
                    .operationName(op.getOperationName())
                    .description(op.getDescription())
                    .setupTimeMinutes(op.getSetupTimeMinutes())
                    .runTimeMinutes(op.getRunTimeMinutes())
                    .queueTimeMinutes(op.getQueueTimeMinutes())
                    .moveTimeMinutes(op.getMoveTimeMinutes())
                    .crewSize(op.getCrewSize())
                    .isOutsourced(op.getIsOutsourced())
                    .vendorId(op.getVendorId())
                    .notes(op.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMExplosionResultDto {
        private UUID componentId;
        private String componentType;
        private BigDecimal quantityPerUnit;
        private BigDecimal totalQuantity;
        private Integer level;
        private String path;
        private Boolean isPhantom;
        private UUID bomId;
        private Integer position;
        private String unitOfMeasure;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBOMRequest {
        private UUID productId;
        private String type;
        private String version;
        private String description;
        private BigDecimal baseQuantity;
        private BigDecimal scrapPercentage;
        private Boolean isPhantom;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
        private List<CreateBOMItemRequest> items;
        private List<CreateBOMOperationRequest> operations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBOMItemRequest {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBOMOperationRequest {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExplodeBOMRequest {
        private BigDecimal productionQuantity;
        private Boolean includePhantoms;
    }
}