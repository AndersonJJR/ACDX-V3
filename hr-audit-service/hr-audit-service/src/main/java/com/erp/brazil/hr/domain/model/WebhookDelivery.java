package com.erp.brazil.hr.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebhookDelivery(
    Long id,
    UUID uuid,
    Long subscriptionId,
    String eventType,
    Map<String, Object> payload,
    Integer responseStatus,
    String responseBody,
    Map<String, String> responseHeaders,
    int attempt,
    DeliveryStatus status,
    String errorMessage,
    Instant nextRetryAt,
    Instant sentAt
) {
    public enum DeliveryStatus {
        PENDING, SENT, SUCCESS, FAILED, RETRYING, DEAD_LETTER
    }

    public boolean isTerminal() {
        return status == DeliveryStatus.SUCCESS || status == DeliveryStatus.DEAD_LETTER;
    }

    public boolean canRetry() {
        return status == DeliveryStatus.FAILED || status == DeliveryStatus.RETRYING;
    }
}