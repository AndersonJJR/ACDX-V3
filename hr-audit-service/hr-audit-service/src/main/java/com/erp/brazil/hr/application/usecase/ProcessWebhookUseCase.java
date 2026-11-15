package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessWebhookUseCase {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WebhookDeliveryService deliveryService;

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long INITIAL_RETRY_DELAY_MS = 2000;
    private static final double RETRY_MULTIPLIER = 2.0;
    private static final long MAX_RETRY_DELAY_MS = 30000;

    @Transactional
    public void publishEvent(String eventType, Map<String, Object> payload, Long companyId) {
        List<WebhookSubscription> subscriptions = subscriptionRepository.findByCompanyIdAndEvent(companyId, eventType);

        for (WebhookSubscription subscription : subscriptions) {
            if (!subscription.getIsActive()) continue;

            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setSubscription(subscription);
            delivery.setEventType(eventType);
            delivery.setPayload(payload);
            delivery.setStatus(WebhookDeliveryStatus.PENDING);
            delivery.setAttempt(1);

            deliveryRepository.save(delivery);

            deliverWebhook(delivery.getId());
        }
    }

    @Transactional
    public void deliverWebhook(Long deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        if (delivery.getStatus() == WebhookDelivery.DeliveryStatus.SUCCESS ||
            delivery.getStatus() == WebhookDelivery.DeliveryStatus.DEAD_LETTER) {
            return;
        }

        delivery.setStatus(WebhookDelivery.DeliveryStatus.SENT);
        delivery.setSentAt(Instant.now());
        deliveryRepository.save(delivery);

        try {
            WebhookSubscription subscription = delivery.getSubscription();
            deliveryService.deliver(subscription.getUrl(), subscription.getSecret(),
                delivery.getEventType(), delivery.getPayload());

            delivery.markSuccess();
            subscription.recordSuccess();
            subscriptionRepository.save(subscription);

        } catch (Exception e) {
            log.warn("Webhook delivery failed: {}", e.getMessage());
            handleDeliveryFailure(delivery, e);
        }

        deliveryRepository.save(delivery);
    }

    @Transactional
    public void processRetries() {
        Instant now = Instant.now();
        List<WebhookDelivery> readyForRetry = deliveryRepository.findReadyForRetry(
            WebhookDelivery.DeliveryStatus.RETRYING, now);

        for (WebhookDelivery delivery : readyForRetry) {
            deliverWebhook(delivery.getId());
        }
    }

    private void handleDeliveryFailure(WebhookDelivery delivery, Exception e) {
        WebhookSubscription subscription = delivery.getSubscription();

        if (delivery.getAttempt() >= MAX_RETRY_ATTEMPTS) {
            delivery.markFailed(e.getMessage());
            subscription.recordFailure();

            if (subscription.shouldDisable()) {
                subscription.setIsActive(false);
                log.warn("Webhook subscription {} disabled due to consecutive failures", subscription.getId());
            }
        } else {
            delivery.scheduleRetry(MAX_RETRY_ATTEMPTS, INITIAL_RETRY_DELAY_MS, RETRY_MULTIPLIER, MAX_RETRY_DELAY_MS);
        }

        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void registerSubscription(WebhookSubscription subscription) {
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void updateSubscription(Long subscriptionId, WebhookSubscription updated) {
        WebhookSubscription existing = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        existing.setName(updated.getName());
        existing.setUrl(updated.getUrl());
        existing.setEvents(updated.getEvents());
        existing.setHeaders(updated.getHeaders());
        existing.setIsActive(updated.getIsActive());
        existing.setRetryPolicy(updated.getRetryPolicy());

        subscriptionRepository.save(existing);
    }

    @Transactional
    public void deleteSubscription(Long subscriptionId) {
        subscriptionRepository.deleteById(subscriptionId);
    }

    public interface WebhookDeliveryService {
        void deliver(String url, String secret, String eventType, Map<String, Object> payload);
    }
}