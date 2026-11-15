package com.erp.brazil.hr.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollProcessedEvent extends DomainEvent {

    private Long payrollId;
    private Integer referenceMonth;
    private Integer referenceYear;
    private Integer totalEmployees;
    private BigDecimal totalGrossAmount;
    private BigDecimal totalNetAmount;
    private BigDecimal totalInssAmount;
    private BigDecimal totalIrrfAmount;
    private BigDecimal totalFgtsAmount;
    private List<Long> employeeIds;
    private Long companyId;

    @Override
    public String getEventName() {
        return "PayrollProcessed";
    }
}