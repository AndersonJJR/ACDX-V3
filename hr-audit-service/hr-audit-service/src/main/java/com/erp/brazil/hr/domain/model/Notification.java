package com.erp.brazil.hr.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Notification(
    Long id,
    UUID uuid,
    Long companyId,
    Long templateId,
    RecipientType recipientType,
    Long recipientId,
    String recipientEmail,
    String recipientPhone,
    Channel channel,
    String subject,
    String content,
    Priority priority,
    Status status,
    Instant scheduledAt,
    Instant sentAt,
    Instant deliveredAt,
    Instant readAt,
    Instant failedAt,
    String failureReason,
    int retryCount,
    int maxRetries,
    String externalId,
    Map<String, Object> metadata
) {
    public enum RecipientType {
        EMPLOYEE, MANAGER, HR, ADMIN, EXTERNAL, ALL_EMPLOYEES, DEPARTMENT
    }

    public enum Channel {
        EMAIL, WHATSAPP, SMS, PUSH, IN_APP
    }

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    public enum Status {
        QUEUED, SENDING, SENT, DELIVERED, READ, FAILED, CANCELLED
    }

    public boolean isPending() {
        return status == Status.QUEUED || status == Status.SENDING;
    }

    public boolean isTerminal() {
        return status == Status.DELIVERED || status == Status.READ || status == Status.FAILED || status == Status.CANCELLED;
    }

    public boolean canRetry() {
        return status == Status.FAILED && retryCount < maxRetries;
    }
}