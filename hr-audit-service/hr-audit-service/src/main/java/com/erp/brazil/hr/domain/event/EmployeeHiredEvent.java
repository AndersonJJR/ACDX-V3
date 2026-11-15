package com.erp.brazil.hr.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeHiredEvent extends DomainEvent {

    private Long employeeId;
    private String employeeNumber;
    private String fullName;
    private String cpf;
    private String email;
    private LocalDate hireDate;
    private String positionTitle;
    private String departmentName;
    private String contractType;
    private BigDecimal salaryBase;
    private Long companyId;
    private Long departmentId;
    private Long costCenterId;

    @Override
    public String getEventName() {
        return "EmployeeHired";
    }
}