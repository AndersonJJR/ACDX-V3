package com.erp.brazil.common.domain.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Audit event for tracking changes to entities.
 * Stored in MongoDB for audit trail purposes.
 */
@Getter
@Setter
@ToString
public class AuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tenantId;
    private String entityType;
    private String entityId;
    private AuditAction action;
    private JsonNode oldValue;
    private JsonNode newValue;
    private String changedBy;
    private OffsetDateTime changedAt;
    private String correlationId;
    private String requestIp;
    private String userAgent;

    public AuditEvent() {
        this.id = UUID.randomUUID().toString();
        this.changedAt = OffsetDateTime.now();
    }

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        SOFT_DELETE,
        RESTORE,
        LOGIN,
        LOGOUT,
        FAILED_LOGIN,
        PASSWORD_CHANGE,
        PERMISSION_CHANGE
    }

    public static AuditEvent create(String tenantId, String entityType, String entityId,
                                     String changedBy, JsonNode newValue) {
        AuditEvent event = new AuditEvent();
        event.setTenantId(tenantId);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(AuditAction.CREATE);
        event.setNewValue(newValue);
        event.setChangedBy(changedBy);
        return event;
    }

    public static AuditEvent update(String tenantId, String entityType, String entityId,
                                     String changedBy, JsonNode oldValue, JsonNode newValue) {
        AuditEvent event = new AuditEvent();
        event.setTenantId(tenantId);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(AuditAction.UPDATE);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        event.setChangedBy(changedBy);
        return event;
    }

    public static AuditEvent delete(String tenantId, String entityType, String entityId,
                                     String changedBy, JsonNode oldValue) {
        AuditEvent event = new AuditEvent();
        event.setTenantId(tenantId);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(AuditAction.DELETE);
        event.setOldValue(oldValue);
        event.setChangedBy(changedBy);
        return event;
    }
}