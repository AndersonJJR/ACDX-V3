package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "webhook_delivery",
    uniqueConstraints = @UniqueConstraint(name = "uk_webhook_delivery_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_webhook_delivery_subscription", columnList = "subscription_id"),
        @Index(name = "idx_webhook_delivery_status", columnList = "status"),
        @Index(name = "idx_webhook_delivery_next_retry", columnList = "next_retry_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private WebhookSubscription subscription;

    @NotBlank
    @Size(max = 100)
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @ElementCollection
    @CollectionTable(name = "webhook_delivery_payload", joinColumns = @JoinColumn(name = "delivery_id"))
    @MapKeyColumn(name = "payload_key")
    @Column(name = "payload_value")
    @Builder.Default
    private Map<String, Object> payload = new java.util.HashMap<>();

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @ElementCollection
    @CollectionTable(name = "webhook_delivery_response_headers", joinColumns = @JoinColumn(name = "delivery_id"))
    @MapKeyColumn(name = "header_key")
    @Column(name = "header_value")
    @Builder.Default
    private Map<String, String> responseHeaders = new java.util.HashMap<>();

    @Column(name = "attempt")
    private int attempt = 1;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Size(max = 500)
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    // Business methods
    public void scheduleRetry(int maxRetries, long initialDelayMs, double backoffMultiplier, long maxDelayMs) {
        this.attempt++;
        this.status = DeliveryStatus.RETRYING;
        long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
        this.nextRetryAt = Instant.now().plusMillis(Math.min(delay, maxDelayMs));
    }

    public void markSuccess() {
        this.status = DeliveryStatus.SUCCESS;
        this.sentAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = DeliveryStatus.DEAD_LETTER;
        this.errorMessage = errorMessage;
        this.sentAt = Instant.now();
    }

    public boolean isTerminal() {
        return status == DeliveryStatus.SUCCESS || status == DeliveryStatus.DEAD_LETTER;
    }

    public boolean canRetry() {
        return status == DeliveryStatus.FAILED || status == DeliveryStatus.RETRYING;
    }

    public enum DeliveryStatus {
        PENDING, SENT, SUCCESS, FAILED, RETRYING, DEAD_LETTER
    }
}