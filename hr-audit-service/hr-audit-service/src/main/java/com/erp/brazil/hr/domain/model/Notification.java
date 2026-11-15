package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "notification",
    uniqueConstraints = @UniqueConstraint(name = "uk_notification_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_type,recipient_id"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_notification_company", columnList = "company_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    private RecipientType recipientType;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Size(max = 255)
    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Size(max = 20)
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private NotificationChannel channel;

    @Size(max = 255)
    @Column(name = "subject", length = 255)
    private String subject;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Size(max = 500)
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "max_retries")
    private int maxRetries = 3;

    @Size(max = 100)
    @Column(name = "external_id", length = 100)
    private String externalId;

    @ElementCollection
    @CollectionTable(name = "notification_metadata", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @Builder.Default
    private Map<String, Object> metadata = new java.util.HashMap<>();

    // Business methods
    public boolean isPending() {
        return status == NotificationStatus.QUEUED || status == NotificationStatus.SENDING;
    }

    public boolean isTerminal() {
        return status == NotificationStatus.DELIVERED || status == NotificationStatus.READ || status == NotificationStatus.FAILED || status == NotificationStatus.CANCELLED;
    }

    public boolean canRetry() {
        return status == NotificationStatus.FAILED && retryCount < maxRetries;
    }

    public void markSent(String externalId) {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.externalId = externalId;
    }

    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = NotificationStatus.QUEUED;
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = Instant.now();
        this.failureReason = reason;
    }

    public enum RecipientType {
        EMPLOYEE, MANAGER, HR, ADMIN, EXTERNAL, ALL_EMPLOYEES, DEPARTMENT
    }
}