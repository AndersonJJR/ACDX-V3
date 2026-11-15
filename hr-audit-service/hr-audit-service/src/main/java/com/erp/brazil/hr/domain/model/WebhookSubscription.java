package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "webhook_subscription",
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
    @Column(name = "company_id", nullable = false)
    private Long companyId;

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

    @Type(JsonBinaryType.class)
    @Column(name = "events", columnDefinition = "jsonb", nullable = false)
    private java.util.List<String> events;

    @Type(JsonBinaryType.class)
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, String> headers;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Type(JsonBinaryType.class)
    @Column(name = "retry_policy", columnDefinition = "jsonb")
    private Map<String, Object> retryPolicy;

    @Column(name = "last_triggered_at")
    private java.time.Instant lastTriggeredAt;

    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures = 0;

    public boolean isEventSubscribed(String event) {
        return events != null && events.contains(event);
    }

    public void recordSuccess() {
        this.consecutiveFailures = 0;
        this.lastTriggeredAt = java.time.Instant.now();
    }

    public void recordFailure() {
        this.consecutiveFailures++;
    }

    public boolean shouldDisable() {
        return consecutiveFailures >= 10; // Disable after 10 consecutive failures
    }
}