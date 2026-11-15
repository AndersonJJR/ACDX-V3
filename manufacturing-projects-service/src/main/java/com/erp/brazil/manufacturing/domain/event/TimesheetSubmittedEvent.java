package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetSubmittedEvent {

    private UUID eventId = UUID.randomUUID();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String eventType = "TimesheetSubmitted";

    private UUID timesheetId;
    private UUID projectId;
    private UUID taskId;
    private UUID employeeId;
    private java.time.LocalDate workDate;
    private java.math.BigDecimal hoursWorked;
    private Boolean isBillable;
    private java.math.BigDecimal billingRate;
    private String submittedBy;
}