package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "webhook_subscription",
    uniqueConstraints = @UniqueConstraint(name = "uk_webhook_subscription_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_webhook_company", columnList = "company_id"),
        @Index(name = "idx_webhook_active", columnList = "is_active")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookSubscription extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 500)
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @NotBlank
    @Size(max = 255)
    @Column(name = "secret", nullable = false, length = 255)
    private String secret;

    @ElementCollection
    @CollectionTable(name = "webhook_subscription_events", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "event")
    @Builder.Default
    private List<String> events = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "webhook_subscription_headers", joinColumns = @JoinColumn(name = "subscription_id"))
    @MapKeyColumn(name = "header_key")
    @Column(name = "header_value")
    @Builder.Default
    private Map<String, String> headers = new java.util.HashMap<>();

    @Column(name = "is_active")
    private boolean isActive = true;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "maxRetries", column = @Column(name = "retry_max_retries")),
        @AttributeOverride(name = "initialDelayMs", column = @Column(name = "retry_initial_delay_ms")),
        @AttributeOverride(name = "backoffMultiplier", column = @Column(name = "retry_backoff_multiplier")),
        @AttributeOverride(name = "maxDelayMs", column = @Column(name = "retry_max_delay_ms"))
    })
    private RetryPolicy retryPolicy = RetryPolicy.defaultPolicy();

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    @Column(name = "consecutive_failures")
    private int consecutiveFailures = 0;

    // Business methods
    public void recordSuccess() {
        this.consecutiveFailures = 0;
        this.lastTriggeredAt = Instant.now();
    }

    public void recordFailure() {
        this.consecutiveFailures++;
        this.lastTriggeredAt = Instant.now();
    }

    public boolean shouldDisable() {
        return consecutiveFailures >= 5;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RetryPolicy {
        private int maxRetries = 5;
        private long initialDelayMs = 1000;
        private double backoffMultiplier = 2.0;
        private long maxDelayMs = 300000;

        public static RetryPolicy defaultPolicy() {
            return RetryPolicy.builder().build();
        }

        public long calculateDelay(int attempt) {
            long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
            return Math.min(delay, maxDelayMs);
        }

        public boolean shouldRetry(int attempt) {
            return attempt <= maxRetries;
        }
    }
}