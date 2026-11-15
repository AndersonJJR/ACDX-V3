package com.erp.brazil.manufacturing.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionCreatedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID inspectionId;
    private String inspectionNumber;
    private String inspectionType;
    private UUID productId;
    private UUID referenceId;
    private String referenceType;
    private String inspectorId;
    private Integer sampleSize;
}