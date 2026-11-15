package com.erp.brazil.manufacturing.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSubmittedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID timesheetId;
    private UUID employeeId;
    private UUID projectId;
    private UUID taskId;
    private LocalDate workDate;
    private BigDecimal hoursWorked;
    private BigDecimal overtimeHours;
    private boolean isBillable;
    private String status;
}