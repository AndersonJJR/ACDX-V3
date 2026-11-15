package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityInspectionPassedEvent {

    private UUID eventId = UUID.randomUUID();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String eventType = "QualityInspectionPassed";

    private UUID qualityInspectionId;
    private String inspectionNumber;
    private UUID productId;
    private String lotNumber;
    private java.math.BigDecimal acceptedQuantity;
    private UUID warehouseId;
    private String inspectedBy;
}