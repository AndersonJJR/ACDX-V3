package com.erp.brazil.hr.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WebhookSubscription(
    Long id,
    UUID uuid,
    Long companyId,
    String name,
    String url,
    String secret,
    List<String> events,
    Map<String, String> headers,
    boolean isActive,
    RetryPolicy retryPolicy,
    Instant lastTriggeredAt,
    int consecutiveFailures
) {
    public record RetryPolicy(
        int maxRetries,
        long initialDelayMs,
        double backoffMultiplier,
        long maxDelayMs
    ) {
        public static RetryPolicy defaultPolicy() {
            return new RetryPolicy(5, 1000, 2.0, 300000);
        }

        public long calculateDelay(int attempt) {
            long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
            return Math.min(delay, maxDelayMs);
        }
    }

    public boolean shouldRetry(int attempt) {
        return attempt <= retryPolicy.maxRetries();
    }

    public long nextRetryDelay(int attempt) {
        return retryPolicy.calculateDelay(attempt);
    }
}