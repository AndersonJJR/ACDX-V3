package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.AuditLog;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AuditLogDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String entityId;
        private String entityType;
        private Long companyId;
        private String userId;
        private String action;
        private Instant timestamp;
        private String ipAddress;
        private String userAgent;
        private String requestId;
        private String sessionId;
        private Map<String, Object> oldValues;
        private Map<String, Object> newValues;
        private List<String> changedFields;
        private Map<String, Object> metadata;
        private String riskLevel;
        private List<String> complianceTags;
        private Boolean isMasked;
        private List<String> maskedFields;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String entityId;
        private String entityType;
        private Long companyId;
        private String userId;
        private String action;
        private Instant startDate;
        private Instant endDate;
        private String riskLevel;
        private List<String> complianceTags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DSARRequest {
        private String subjectId;
        private String subjectType; // EMPLOYEE, CANDIDATE, etc.
        private String requestType; // ACCESS, RECTIFICATION, ERASURE, PORTABILITY
        private String requesterEmail;
        private String justification;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DSARResponse {
        private String requestId;
        private String status;
        private Instant requestedAt;
        private Instant dueDate;
        private String message;
    }
}