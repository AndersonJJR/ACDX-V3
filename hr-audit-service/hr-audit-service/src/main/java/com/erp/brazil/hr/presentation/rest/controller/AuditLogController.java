package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.domain.model.AuditLog;
import com.erp.brazil.hr.domain.repository.AuditLogRepository;
import com.erp.brazil.hr.presentation.rest.dto.AuditLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "LGPD Audit trail APIs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search audit logs")
    public ResponseEntity<AuditLogDto.PageResponse> search(
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) List<String> complianceTags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Instant start = startDate != null ? startDate : Instant.now().minusSeconds(86400 * 30); // 30 days
        Instant end = endDate != null ? endDate : Instant.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.findByCompanyIdAndTimestampBetween(companyId != null ? companyId : 1L, start, end, pageable);

        return ResponseEntity.ok(AuditLogDto.PageResponse.builder()
            .content(logs.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
            .page(logs.getNumber())
            .size(logs.getSize())
            .totalElements(logs.getTotalElements())
            .totalPages(logs.getTotalPages())
            .first(logs.isFirst())
            .last(logs.isLast())
            .build());
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit trail for entity")
    public ResponseEntity<List<AuditLogDto.Response>> getEntityTrail(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.findByEntityIdAndEntityType(entityId, entityType, pageable);

        return ResponseEntity.ok(logs.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user activity")
    public ResponseEntity<AuditLogDto.PageResponse> getUserActivity(
            @PathVariable String userId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate, pageable);

        return ResponseEntity.ok(AuditLogDto.PageResponse.builder()
            .content(logs.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
            .page(logs.getNumber())
            .size(logs.getSize())
            .totalElements(logs.getTotalElements())
            .totalPages(logs.getTotalPages())
            .first(logs.isFirst())
            .last(logs.isLast())
            .build());
    }

    @PostMapping("/dsar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process DSAR request (LGPD)")
    public ResponseEntity<AuditLogDto.DSARResponse> processDsar(@RequestBody AuditLogDto.DSARRequest request) {
        // Would implement DSAR processing
        return ResponseEntity.ok(AuditLogDto.DSARResponse.builder()
            .requestId(java.util.UUID.randomUUID().toString())
            .status("RECEIVED")
            .requestedAt(Instant.now())
            .dueDate(Instant.now().plusSeconds(86400 * 15)) // 15 days
            .message("DSAR request received and queued for processing")
            .build());
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export audit logs")
    public ResponseEntity<byte[]> export(
            @RequestParam Long companyId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {

        // Would generate CSV/Excel export
        return ResponseEntity.ok().build();
    }

    private AuditLogDto.Response toResponse(AuditLog log) {
        return AuditLogDto.Response.builder()
            .id(log.getId())
            .entityId(log.getEntityId())
            .entityType(log.getEntityType())
            .companyId(log.getCompanyId())
            .userId(log.getUserId())
            .action(log.getAction())
            .timestamp(log.getTimestamp())
            .ipAddress(log.getIpAddress())
            .userAgent(log.getUserAgent())
            .requestId(log.getRequestId())
            .sessionId(log.getSessionId())
            .oldValues(log.getOldValues())
            .newValues(log.getNewValues())
            .changedFields(log.getChangedFields())
            .metadata(log.getMetadata())
            .riskLevel(log.getRiskLevel() != null ? log.getRiskLevel().name() : null)
            .complianceTags(log.getComplianceTags())
            .isMasked(log.getIsMasked())
            .maskedFields(log.getMaskedFields())
            .build();
    }
}