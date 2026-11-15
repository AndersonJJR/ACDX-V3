package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.AuditLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

public record AuditLogDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) String id,
    String eventId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant timestamp,
    Long companyId,
    Long userId,
    String entityType,
    String entityId,
    String action,
    Map<String, Object> oldValue,
    Map<String, Object> newValue,
    String ipAddress,
    String userAgent,
    String correlationId,
    String sessionId
) {
    public static AuditLogDTO from(AuditLog log) {
        return new AuditLogDTO(
            log.id(), log.eventId(), log.timestamp(), log.companyId(), log.userId(),
            log.entityType(), log.entityId(), log.action(), log.oldValue(), log.newValue(),
            log.ipAddress(), log.userAgent(), log.correlationId(), log.sessionId()
        );
    }
}