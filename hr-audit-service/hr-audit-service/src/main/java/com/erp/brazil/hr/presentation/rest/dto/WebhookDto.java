package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.WebhookDeliveryStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class WebhookDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionRequest {
        private String name;
        private String url;
        private List<String> events;
        private Map<String, String> headers;
        private Map<String, Object> retryPolicy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionResponse {
        private Long id;
        private String uuid;
        private Long companyId;
        private String name;
        private String url;
        private List<String> events;
        private Map<String, String> headers;
        private Boolean isActive;
        private Map<String, Object> retryPolicy;
        private Instant lastTriggeredAt;
        private Integer consecutiveFailures;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryResponse {
        private Long id;
        private String uuid;
        private Long subscriptionId;
        private String subscriptionName;
        private String eventType;
        private Map<String, Object> payload;
        private Integer responseStatus;
        private String responseBody;
        private Map<String, String> responseHeaders;
        private Integer attempt;
        private WebhookDeliveryStatus status;
        private String statusDescription;
        private String errorMessage;
        private Instant nextRetryAt;
        private Instant sentAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublishEventRequest {
        private String eventType;
        private Map<String, Object> payload;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<DeliveryResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}