package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.AuditLog;
import com.erp.brazil.hr.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditEventUseCase {

    private final AuditLogRepository auditLogRepository;

    public AuditEventUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(String action, String entityType, String entityId,
                         Long companyId, Long userId, Object newValue) {
        logEvent(action, entityType, entityId, companyId, userId, null, newValue, null, null, null);
    }

    public void logEvent(String action, String entityType, String entityId,
                         Long companyId, Long userId, Object oldValue, Object newValue,
                         String ipAddress, String userAgent, String correlationId) {
        try {
            Map<String, Object> oldMap = oldValue != null ? toMap(oldValue) : null;
            Map<String, Object> newMap = newValue != null ? toMap(newValue) : null;

            AuditLog log = AuditLog.create(
                UUID.randomUUID().toString(), companyId, userId, entityType, entityId,
                action, oldMap, newMap, ipAddress, userAgent, correlationId, null
            );

            AuditLog maskedLog = log.withMaskedValues();
            auditLogRepository.save(maskedLog);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        if (obj instanceof com.erp.brazil.hr.domain.model.Employee emp) {
            return Map.of(
                "id", emp.id(), "uuid", emp.uuid(), "companyId", emp.companyId(),
                "employeeNumber", emp.employeeNumber(), "fullName", emp.fullName(),
                "cpf", emp.cpf(), "email", emp.email(), "status", emp.status(),
                "hireDate", emp.hireDate(), "terminationDate", emp.terminationDate()
            );
        }
        // Add more entity types as needed
        return Map.of("value", obj.toString());
    }
}