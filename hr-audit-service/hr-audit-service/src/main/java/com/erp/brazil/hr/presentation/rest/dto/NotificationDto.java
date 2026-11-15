package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.NotificationChannel;
import com.erp.brazil.hr.domain.model.NotificationPriority;
import com.erp.brazil.hr.domain.model.NotificationStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class NotificationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateRequest {
        private String code;
        private String name;
        private NotificationChannel channel;
        private String subject;
        private String contentTemplate;
        private String contentType;
        private List<String> variables;
        private String locale;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateResponse {
        private Long id;
        private String uuid;
        private Long companyId;
        private String code;
        private String name;
        private NotificationChannel channel;
        private String subject;
        private String contentTemplate;
        private String contentType;
        private List<String> variables;
        private String locale;
        private Boolean isActive;
        private Boolean isSystem;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendNotificationRequest {
        private String templateCode;
        private String locale;
        private RecipientType recipientType;
        private Long recipientId;
        private String recipientEmail;
        private String recipientPhone;
        private Map<String, Object> variables;
        private Instant scheduledAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImmediateNotificationRequest {
        private NotificationChannel channel;
        private String recipientEmail;
        private String recipientPhone;
        private String subject;
        private String content;
        private NotificationPriority priority;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationResponse {
        private Long id;
        private String uuid;
        private Long companyId;
        private Long templateId;
        private String templateCode;
        private RecipientType recipientType;
        private Long recipientId;
        private String recipientEmail;
        private String recipientPhone;
        private NotificationChannel channel;
        private String subject;
        private String content;
        private NotificationPriority priority;
        private NotificationStatus status;
        private Instant scheduledAt;
        private Instant sentAt;
        private Instant deliveredAt;
        private Instant readAt;
        private Instant failedAt;
        private String failureReason;
        private Integer retryCount;
        private Integer maxRetries;
        private String externalId;
        private Map<String, Object> metadata;
    }

    public enum RecipientType {
        EMPLOYEE, MANAGER, HR, ADMIN, EXTERNAL, ALL_EMPLOYEES, DEPARTMENT
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<NotificationResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}