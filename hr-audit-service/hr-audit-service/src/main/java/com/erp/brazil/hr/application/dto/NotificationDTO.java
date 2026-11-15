package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long companyId,
    Long templateId,
    Notification.RecipientType recipientType,
    Long recipientId,
    String recipientEmail,
    String recipientPhone,
    Notification.Channel channel,
    String subject,
    String content,
    Notification.Priority priority,
    Notification.Status status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant scheduledAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant sentAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant deliveredAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant readAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant failedAt,
    String failureReason,
    int retryCount,
    int maxRetries,
    String externalId,
    Map<String, Object> metadata
) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
            notification.uuid(), notification.companyId(), notification.templateId(), notification.recipientType(),
            notification.recipientId(), notification.recipientEmail(), notification.recipientPhone(),
            notification.channel(), notification.subject(), notification.content(), notification.priority(),
            notification.status(), notification.scheduledAt(), notification.sentAt(), notification.deliveredAt(),
            notification.readAt(), notification.failedAt(), notification.failureReason(), notification.retryCount(),
            notification.maxRetries(), notification.externalId(), notification.metadata()
        );
    }
}