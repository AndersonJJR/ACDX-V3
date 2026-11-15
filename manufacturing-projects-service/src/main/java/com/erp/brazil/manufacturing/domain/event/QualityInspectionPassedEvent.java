package com.erp.brazil.manufacturing.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionPassedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID inspectionId;
    private String inspectionNumber;
    private UUID productId;
    private BigDecimal acceptedQuantity;
    private UUID referenceId;
    private String referenceType;
    private UUID warehouseId;
}