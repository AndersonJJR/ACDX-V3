package com.erp.brazil.hr.domain.event;

import lombok.*;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTerminatedEvent extends DomainEvent {

    private Long employeeId;
    private String employeeNumber;
    private String fullName;
    private String cpf;
    private LocalDate terminationDate;
    private String terminationReason;
    private Integer noticePeriodDays;
    private Boolean hasJustCause;
    private Long companyId;

    @Override
    public String getEventName() {
        return "EmployeeTerminated";
    }
}