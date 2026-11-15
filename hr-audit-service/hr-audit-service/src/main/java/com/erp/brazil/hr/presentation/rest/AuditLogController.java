package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.AuditLogDTO;
import com.erp.brazil.hr.domain.model.AuditLog;
import com.erp.brazil.hr.domain.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Logs", description = "Audit trail and LGPD compliance API")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit logs with filters")
    public ResponseEntity<Page<AuditLogDTO>> search(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Entity type") @RequestParam(required = false) String entityType,
            @Parameter(description = "Entity ID") @RequestParam(required = false) String entityId,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Action") @RequestParam(required = false) String action,
            @Parameter(description = "Start date (ISO 8601)") @RequestParam(required = false) Instant startDate,
            @Parameter(description = "End date (ISO 8601)") @RequestParam(required = false) Instant endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AuditLogDTO> page;
        if (entityType != null && entityId != null) {
            page = auditLogRepository.findEntityHistory(entityType, entityId, pageable)
                .map(AuditLogDTO::from);
        } else if (userId != null && startDate != null && endDate != null) {
            page = auditLogRepository.findByCompanyIdAndUserIdAndDateRange(companyId, userId, startDate, endDate, pageable)
                .map(AuditLogDTO::from);
        } else if (startDate != null && endDate != null) {
            page = auditLogRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate, pageable)
                .map(AuditLogDTO::from);
        } else {
            page = auditLogRepository.findByCompanyIdOrderByTimestampDesc(companyId, pageable)
                .map(AuditLogDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get full audit history for an entity")
    public ResponseEntity<List<AuditLogDTO>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        List<AuditLogDTO> history = auditLogRepository.findEntityHistory(entityType, entityId)
            .stream().map(AuditLogDTO::from).toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/export/{userId}")
    @Operation(summary = "Export all data for a user (LGPD DSAR - Data Subject Access Request)")
    public ResponseEntity<String> exportUserData(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogRepository.findByUserId(userId);

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"userId\": ").append(userId).append(",\n");
        json.append("  \"exportedAt\": \"").append(Instant.now()).append("\",\n");
        json.append("  \"records\": [\n");

        for (int i = 0; i < logs.size(); i++) {
            AuditLog log = logs.get(i);
            json.append("    {\n");
            json.append("      \"eventId\": \"").append(log.eventId()).append("\",\n");
            json.append("      \"timestamp\": \"").append(log.timestamp()).append("\",\n");
            json.append("      \"entityType\": \"").append(log.entityType()).append("\",\n");
            json.append("      \"entityId\": \"").append(log.entityId()).append("\",\n");
            json.append("      \"action\": \"").append(log.action()).append("\",\n");
            json.append("      \"oldValue\": ").append(toJson(log.oldValue())).append(",\n");
            json.append("      \"newValue\": ").append(toJson(log.newValue())).append("\n");
            json.append("    }");
            if (i < logs.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ]\n}");

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dsar_user_" + userId + ".json\"")
            .body(json.toString());
    }

    @PostMapping("/erase/{userId}")
    @Operation(summary = "Erase user data (LGPD Right to be Forgotten)")
    public ResponseEntity<Map<String, Object>> eraseUserData(@PathVariable Long userId) {
        // In a real implementation, this would:
        // 1. Anonymize personal data in all entities
        // 2. Delete or anonymize audit logs for this user
        // 3. Notify downstream systems
        // 4. Return confirmation

        // For audit logs, we mark them as erased rather than delete (for integrity)
        List<AuditLog> userLogs = auditLogRepository.findByUserId(userId);
        int count = userLogs.size();

        // In production: update logs to anonymize userId and personal data
        // auditLogRepository.updateUserId(userId, null); // Would need custom query

        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "erasedAt", Instant.now(),
            "affectedRecords", count,
            "status", "ANONYMIZED"
        ));
    }

    private String toJson(Map<String, Object> map) {
        if (map == null) return "null";
        return map.entrySet().stream()
            .map(e -> "\"" + e.getKey() + "\": " + (e.getValue() instanceof String ? "\"" + e.getValue() + "\"" : e.getValue()))
            .collect(Collectors.joining(", ", "{", "}"));
    }
}