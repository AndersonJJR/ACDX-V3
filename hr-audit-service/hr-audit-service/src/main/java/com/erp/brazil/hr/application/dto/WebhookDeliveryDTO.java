package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.WebhookDelivery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebhookDeliveryDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long subscriptionId,
    String eventType,
    Map<String, Object> payload,
    Integer responseStatus,
    String responseBody,
    Map<String, String> responseHeaders,
    int attempt,
    WebhookDelivery.DeliveryStatus status,
    String errorMessage,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant nextRetryAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Instant sentAt
) {
    public static WebhookDeliveryDTO from(WebhookDelivery delivery) {
        return new WebhookDeliveryDTO(
            delivery.uuid(), delivery.subscriptionId(), delivery.eventType(), delivery.payload(),
            delivery.responseStatus(), delivery.responseBody(), delivery.responseHeaders(), delivery.attempt(),
            delivery.status(), delivery.errorMessage(), delivery.nextRetryAt(), delivery.sentAt()
        );
    }
}