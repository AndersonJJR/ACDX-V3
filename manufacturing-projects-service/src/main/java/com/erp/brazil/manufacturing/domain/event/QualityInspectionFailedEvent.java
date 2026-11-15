package com.erp.brazil.manufacturing.domain.event;

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
public class QualityInspectionFailedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID inspectionId;
    private String inspectionNumber;
    private UUID productId;
    private BigDecimal rejectedQuantity;
    private String disposition;
    private UUID referenceId;
    private String referenceType;
    private List<UUID> nonConformanceIds;
}