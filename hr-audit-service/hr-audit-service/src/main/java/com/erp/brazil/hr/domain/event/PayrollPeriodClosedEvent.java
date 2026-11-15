package com.erp.brazil.hr.domain.event;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodClosedEvent extends DomainEvent {

    private Long payrollId;
    private Integer referenceMonth;
    private Integer referenceYear;
    private Long companyId;
    private List<Long> employeeIds;

    @Override
    public String getEventName() {
        return "PayrollPeriodClosed";
    }
}