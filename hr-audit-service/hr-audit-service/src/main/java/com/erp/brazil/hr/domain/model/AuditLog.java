package com.erp.brazil.hr.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "audit_logs")
public record AuditLog(
    @Id String id,
    String eventId,
    @Indexed Instant timestamp,
    @Indexed Long companyId,
    @Indexed Long userId,
    @Indexed String entityType,
    @Indexed String entityId,
    String action,
    Map<String, Object> oldValue,
    Map<String, Object> newValue,
    String ipAddress,
    String userAgent,
    String correlationId,
    String sessionId
) {
    public static AuditLog create(String eventId, Long companyId, Long userId, String entityType, String entityId,
                                  String action, Map<String, Object> oldValue, Map<String, Object> newValue,
                                  String ipAddress, String userAgent, String correlationId, String sessionId) {
        return new AuditLog(
            null, eventId, Instant.now(), companyId, userId, entityType, entityId,
            action, oldValue, newValue, ipAddress, userAgent, correlationId, sessionId
        );
    }

    public AuditLog withMaskedValues() {
        Map<String, Object> maskedOld = oldValue != null ? maskSensitiveFields(oldValue) : null;
        Map<String, Object> maskedNew = newValue != null ? maskSensitiveFields(newValue) : null;
        return new AuditLog(id, eventId, timestamp, companyId, userId, entityType, entityId,
                          action, maskedOld, maskedNew, ipAddress, userAgent, correlationId, sessionId);
    }

    private Map<String, Object> maskSensitiveFields(Map<String, Object> values) {
        return values.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> maskValue(e.getKey(), e.getValue())
            ));
    }

    private Object maskValue(String key, Object value) {
        if (value == null) return null;
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("cpf") || lowerKey.contains("cnpj")) {
            String str = value.toString();
            if (str.length() == 14) return "***.***.***-**";
            if (str.length() == 18) return "**.***.***/****-**";
            return "**MASKED**";
        }
        if (lowerKey.contains("salary") || lowerKey.contains("salario") || lowerKey.contains("remuneration")) {
            return "**.***";
        }
        if (lowerKey.contains("password") || lowerKey.contains("secret") || lowerKey.contains("token")) {
            return "**MASKED**";
        }
        if (lowerKey.contains("email")) {
            String email = value.toString();
            int at = email.indexOf('@');
            if (at > 2) return email.substring(0, 2) + "***" + email.substring(at);
        }
        if (lowerKey.contains("phone") || lowerKey.contains("telefone")) {
            return "***-***-****";
        }
        return value;
    }
}