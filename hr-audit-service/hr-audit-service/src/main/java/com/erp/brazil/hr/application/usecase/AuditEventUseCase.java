package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.AuditLog;
import com.erp.brazil.hr.domain.repository.AuditLogRepository;
import com.erp.brazil.hr.domain.event.DomainEvent;
import com.erp.brazil.hr.domain.event.EmployeeHiredEvent;
import com.erp.brazil.hr.domain.event.EmployeeTerminatedEvent;
import com.erp.brazil.hr.domain.event.PayrollProcessedEvent;
import com.erp.brazil.hr.domain.event.LeaveApprovedEvent;
import com.erp.brazil.hr.domain.event.PayrollPeriodClosedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventUseCase {

    private final AuditLogRepository auditLogRepository;
    private final DataMaskingService dataMaskingService;

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handleEmployeeHired(EmployeeHiredEvent event) {
        logAudit(event.getEventName(), event.getAggregateId(), "Employee",
            event.getCompanyId(), event.getTriggeredBy(), Map.of(
                "employeeNumber", event.getEmployeeNumber(),
                "fullName", event.getFullName(),
                "cpf", event.getCpf(),
                "email", event.getEmail(),
                "hireDate", event.getHireDate(),
                "positionTitle", event.getPositionTitle(),
                "departmentName", event.getDepartmentName(),
                "contractType", event.getContractType(),
                "salaryBase", event.getSalaryBase()
            ), AuditLog.RiskLevel.LOW, List.of("LGPD", "HR"));
    }

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handleEmployeeTerminated(EmployeeTerminatedEvent event) {
        logAudit(event.getEventName(), event.getAggregateId(), "Employee",
            event.getCompanyId(), event.getTriggeredBy(), Map.of(
                "employeeNumber", event.getEmployeeNumber(),
                "fullName", event.getFullName(),
                "cpf", event.getCpf(),
                "terminationDate", event.getTerminationDate(),
                "terminationReason", event.getTerminationReason(),
                "noticePeriodDays", event.getNoticePeriodDays(),
                "hasJustCause", event.getHasJustCause()
            ), AuditLog.RiskLevel.HIGH, List.of("LGPD", "HR", "TERMINATION"));
    }

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handlePayrollProcessed(PayrollProcessedEvent event) {
        logAudit(event.getEventName(), event.getAggregateId().toString(), "Payroll",
            event.getCompanyId(), event.getTriggeredBy(), Map.of(
                "referenceMonth", event.getReferenceMonth(),
                "referenceYear", event.getReferenceYear(),
                "totalEmployees", event.getTotalEmployees(),
                "totalGrossAmount", event.getTotalGrossAmount(),
                "totalNetAmount", event.getTotalNetAmount(),
                "totalInssAmount", event.getTotalInssAmount(),
                "totalIrrfAmount", event.getTotalIrrfAmount(),
                "totalFgtsAmount", event.getTotalFgtsAmount()
            ), AuditLog.RiskLevel.MEDIUM, List.of("PAYROLL", "FINANCIAL"));
    }

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handleLeaveApproved(LeaveApprovedEvent event) {
        logAudit(event.getEventName(), event.getAggregateId().toString(), "LeaveRequest",
            event.getCompanyId(), event.getTriggeredBy(), Map.of(
                "employeeId", event.getEmployeeId(),
                "employeeName", event.getEmployeeName(),
                "leaveTypeCode", event.getLeaveTypeCode(),
                "leaveTypeName", event.getLeaveTypeName(),
                "startDate", event.getStartDate(),
                "endDate", event.getEndDate(),
                "totalDays", event.getTotalDays(),
                "approvalNotes", event.getApprovalNotes()
            ), AuditLog.RiskLevel.LOW, List.of("HR", "LEAVE"));
    }

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handlePayrollPeriodClosed(PayrollPeriodClosedEvent event) {
        logAudit(event.getEventName(), event.getAggregateId().toString(), "Payroll",
            event.getCompanyId(), event.getTriggeredBy(), Map.of(
                "referenceMonth", event.getReferenceMonth(),
                "referenceYear", event.getReferenceYear(),
                "employeeCount", event.getEmployeeIds().size()
            ), AuditLog.RiskLevel.MEDIUM, List.of("PAYROLL", "PERIOD_CLOSE"));
    }

    @Async("auditEventExecutor")
    @EventListener
    @Transactional
    public void handleGenericDomainEvent(DomainEvent event) {
        if (event instanceof EmployeeHiredEvent ||
            event instanceof EmployeeTerminatedEvent ||
            event instanceof PayrollProcessedEvent ||
            event instanceof LeaveApprovedEvent ||
            event instanceof PayrollPeriodClosedEvent) {
            return; // Already handled by specific listeners
        }

        logAudit(event.getEventName(), event.getAggregateId(), event.getClass().getSimpleName(),
            event.getCompanyId(), event.getTriggeredBy(), event.getMetadata(),
            AuditLog.RiskLevel.LOW, List.of("GENERIC"));
    }

    public void logEvent(String action, String entityType, String entityId, Long companyId,
                         String userId, Object oldValue, Object newValue,
                         String ipAddress, String userAgent, String correlationId) {

        Map<String, Object> maskedNewValues = dataMaskingService.maskSensitiveData(
            newValue instanceof Map ? (Map<String, Object>) newValue : Map.of());
        List<String> maskedFields = dataMaskingService.getMaskedFields(
            newValue instanceof Map ? (Map<String, Object>) newValue : Map.of());

        AuditLog auditLog = AuditLog.builder()
            .id(UUID.randomUUID().toString())
            .entityId(entityId)
            .entityType(entityType)
            .companyId(companyId)
            .userId(userId)
            .action(action)
            .timestamp(Instant.now())
            .newValues(maskedNewValues)
            .changedFields(maskedNewValues.keySet().stream().toList())
            .riskLevel(AuditLog.RiskLevel.LOW)
            .complianceTags(List.of("AUDIT"))
            .isMasked(true)
            .maskedFields(maskedFields)
            .build();

        auditLogRepository.save(auditLog);
    }

    public interface DataMaskingService {
        Map<String, Object> maskSensitiveData(Map<String, Object> data);
        List<String> getMaskedFields(Map<String, Object> data);
    }
}