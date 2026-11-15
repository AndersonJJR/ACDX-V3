package com.erp.brazil.hr.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApprovedEvent extends DomainEvent {

    private Long leaveRequestId;
    private Long employeeId;
    private String employeeName;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String approvalNotes;
    private Long approvedBy;
    private Long companyId;

    @Override
    public String getEventName() {
        return "LeaveApproved";
    }
}