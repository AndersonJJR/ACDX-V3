package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class ProcessWebhookUseCase {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookDeliveryService deliveryService;
    private final AuditEventUseCase auditEventUseCase;

    public ProcessWebhookUseCase(WebhookSubscriptionRepository subscriptionRepository,
                                  WebhookDeliveryRepository deliveryRepository,
                                  WebhookDeliveryService deliveryService,
                                  AuditEventUseCase auditEventUseCase) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryService = deliveryService;
        this.auditEventUseCase = auditEventUseCase;
    }

    public void triggerEvent(String eventType, Map<String, Object> payload, Long companyId) {
        List<WebhookSubscription> subscriptions = subscriptionRepository.findByCompanyIdAndIsActiveTrue(companyId);
        subscriptions = subscriptions.stream()
            .filter(s -> s.events().contains(eventType) || s.events().contains("*"))
            .toList();

        for (WebhookSubscription subscription : subscriptions) {
            createDelivery(subscription, eventType, payload);
        }
    }

    @Transactional
    public WebhookDelivery createDelivery(WebhookSubscription subscription, String eventType, Map<String, Object> payload) {
        WebhookDelivery delivery = new WebhookDelivery(
            null, UUID.randomUUID(), subscription.id(), eventType, payload,
            null, null, null, 1, WebhookDelivery.DeliveryStatus.PENDING, null, null, null
        );

        WebhookDelivery saved = deliveryRepository.save(delivery);
        deliverAsync(saved, subscription);
        return saved;
    }

    private void deliverAsync(WebhookDelivery delivery, WebhookSubscription subscription) {
        CompletableFuture.runAsync(() -> {
            try {
                deliveryRepository.save(new WebhookDelivery(
                    delivery.id(), delivery.uuid(), delivery.subscriptionId(), delivery.eventType(),
                    delivery.payload(), delivery.responseStatus(), delivery.responseBody(),
                    delivery.responseHeaders(), delivery.attempt(), WebhookDelivery.DeliveryStatus.SENT,
                    delivery.errorMessage(), delivery.nextRetryAt(), Instant.now()
                ));

                WebhookDeliveryService.DeliveryResult result = deliveryService.deliver(subscription, delivery);

                if (result.success()) {
                    WebhookDelivery success = new WebhookDelivery(
                        delivery.id(), delivery.uuid(), delivery.subscriptionId(), delivery.eventType(),
                        delivery.payload(), result.statusCode(), result.body(), result.headers(),
                        delivery.attempt(), WebhookDelivery.DeliveryStatus.SUCCESS, null, null, Instant.now()
                    );
                    deliveryRepository.save(success);
                    auditEventUseCase.logEvent("WEBHOOK_DELIVERED", "WebhookDelivery", delivery.uuid().toString(),
                        subscription.companyId(), null, success);
                } else {
                    handleFailure(delivery, subscription, result);
                }
            } catch (Exception e) {
                handleFailure(delivery, subscription, new WebhookDeliveryService.DeliveryResult(false, null, e.getMessage(), null));
            }
        });
    }

    private void handleFailure(WebhookDelivery delivery, WebhookSubscription subscription, WebhookDeliveryService.DeliveryResult result) {
        int nextAttempt = delivery.attempt() + 1;
        boolean shouldRetry = subscription.shouldRetry(nextAttempt);
        long delayMs = subscription.nextRetryDelay(nextAttempt);
        Instant nextRetryAt = shouldRetry ? Instant.now().plusMillis(delayMs) : null;

        WebhookDelivery.DeliveryStatus newStatus = shouldRetry ? WebhookDelivery.DeliveryStatus.RETRYING : WebhookDelivery.DeliveryStatus.DEAD_LETTER;

        WebhookDelivery failed = new WebhookDelivery(
            delivery.id(), delivery.uuid(), delivery.subscriptionId(), delivery.eventType(),
            delivery.payload(), result.statusCode(), result.body(), result.headers(),
            nextAttempt, newStatus, result.errorMessage(), nextRetryAt, delivery.sentAt()
        );

        deliveryRepository.save(failed);
        auditEventUseCase.logEvent("WEBHOOK_FAILED", "WebhookDelivery", delivery.uuid().toString(),
            subscription.companyId(), null, failed);

        if (shouldRetry) {
            // Schedule retry - in production, use a scheduler
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
                retryDelivery(failed, subscription);
            });
        }
    }

    @Transactional
    public void retryDelivery(WebhookDelivery delivery, WebhookSubscription subscription) {
        WebhookDelivery retry = new WebhookDelivery(
            delivery.id(), delivery.uuid(), delivery.subscriptionId(), delivery.eventType(),
            delivery.payload(), delivery.responseStatus(), delivery.responseBody(),
            delivery.responseHeaders(), delivery.attempt(), WebhookDelivery.DeliveryStatus.PENDING,
            delivery.errorMessage(), null, delivery.sentAt()
        );

        deliveryRepository.save(retry);
        deliverAsync(retry, subscription);
    }

    @Transactional
    public void processRetries() {
        List<WebhookDelivery> retryable = deliveryRepository.findRetryableDeliveries(
            WebhookDelivery.DeliveryStatus.RETRYING, Instant.now());

        for (WebhookDelivery delivery : retryable) {
            subscriptionRepository.findById(delivery.subscriptionId()).ifPresent(subscription -> {
                retryDelivery(delivery, subscription);
            });
        }
    }
}