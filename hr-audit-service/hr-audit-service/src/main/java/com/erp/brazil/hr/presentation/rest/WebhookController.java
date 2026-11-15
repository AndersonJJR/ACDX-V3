package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.WebhookDeliveryDTO;
import com.erp.brazil.hr.application.dto.WebhookSubscriptionDTO;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.WebhookDelivery;
import com.erp.brazil.hr.domain.model.WebhookSubscription;
import com.erp.brazil.hr.domain.repository.WebhookDeliveryRepository;
import com.erp.brazil.hr.domain.repository.WebhookSubscriptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Webhook subscription and delivery management")
public class WebhookController {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ProcessWebhookUseCase webhookUseCase;

    public WebhookController(WebhookSubscriptionRepository subscriptionRepository,
                              WebhookDeliveryRepository deliveryRepository,
                              ProcessWebhookUseCase webhookUseCase) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Create a new webhook subscription")
    public ResponseEntity<WebhookSubscriptionDTO> createSubscription(
            @Valid @RequestBody WebhookSubscriptionDTO dto) {
        WebhookSubscription subscription = new WebhookSubscription(
            null, UUID.randomUUID(), dto.companyId(), dto.name(), dto.url(), dto.secret(),
            dto.events(), dto.headers(), true, dto.retryPolicy() != null ? dto.retryPolicy() : WebhookSubscription.RetryPolicy.defaultPolicy(),
            null, 0
        );

        WebhookSubscription saved = subscriptionRepository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebhookSubscriptionDTO.from(saved));
    }

    @GetMapping("/subscriptions/{id}")
    @Operation(summary = "Get webhook subscription by ID")
    public ResponseEntity<WebhookSubscriptionDTO> getSubscription(@PathVariable UUID id) {
        return subscriptionRepository.findByUuid(id)
            .map(WebhookSubscriptionDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "List webhook subscriptions")
    public ResponseEntity<Page<WebhookSubscriptionDTO>> listSubscriptions(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Active only") @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WebhookSubscriptionDTO> page;
        if (activeOnly) {
            page = subscriptionRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable)
                .map(WebhookSubscriptionDTO::from);
        } else {
            page = subscriptionRepository.findByCompanyId(companyId, pageable)
                .map(WebhookSubscriptionDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping("/subscriptions/{id}")
    @Operation(summary = "Update webhook subscription")
    public ResponseEntity<WebhookSubscriptionDTO> updateSubscription(
            @PathVariable UUID id, @Valid @RequestBody WebhookSubscriptionDTO dto) {
        WebhookSubscription existing = subscriptionRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));

        WebhookSubscription updated = new WebhookSubscription(
            existing.id(), existing.uuid(), existing.companyId(), dto.name(), dto.url(),
            dto.secret(), dto.events(), dto.headers(), dto.isActive(), dto.retryPolicy(),
            existing.lastTriggeredAt(), existing.consecutiveFailures()
        );

        WebhookSubscription saved = subscriptionRepository.save(updated);
        return ResponseEntity.ok(WebhookSubscriptionDTO.from(saved));
    }

    @DeleteMapping("/subscriptions/{id}")
    @Operation(summary = "Delete webhook subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
        subscriptionRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
        subscriptionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test/{id}")
    @Operation(summary = "Test webhook delivery")
    public ResponseEntity<WebhookDeliveryDTO> testWebhook(@PathVariable UUID id) {
        WebhookSubscription subscription = subscriptionRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));

        Map<String, Object> testPayload = Map.of(
            "event", "webhook.test",
            "timestamp", java.time.Instant.now(),
            "message", "This is a test webhook delivery"
        );

        WebhookDelivery delivery = webhookUseCase.createDelivery(subscription, "webhook.test", testPayload);
        return ResponseEntity.ok(WebhookDeliveryDTO.from(delivery));
    }

    @GetMapping("/deliveries")
    @Operation(summary = "List webhook deliveries")
    public ResponseEntity<Page<WebhookDeliveryDTO>> listDeliveries(
            @Parameter(description = "Subscription ID") @RequestParam Long subscriptionId,
            @Parameter(description = "Status") @RequestParam(required = false) WebhookDelivery.DeliveryStatus status,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<WebhookDeliveryDTO> page;
        if (status != null) {
            page = deliveryRepository.findBySubscriptionIdAndStatus(subscriptionId, status, pageable)
                .map(WebhookDeliveryDTO::from);
        } else {
            page = deliveryRepository.findBySubscriptionId(subscriptionId, pageable)
                .map(WebhookDeliveryDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @PostMapping("/deliveries/{id}/retry")
    @Operation(summary = "Retry a failed webhook delivery")
    public ResponseEntity<Void> retryDelivery(@PathVariable UUID id) {
        WebhookDelivery delivery = deliveryRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));

        if (!delivery.canRetry()) {
            throw new IllegalStateException("Delivery cannot be retried");
        }

        subscriptionRepository.findById(delivery.subscriptionId()).ifPresent(subscription -> {
            webhookUseCase.retryDelivery(delivery, subscription);
        });

        return ResponseEntity.accepted().build();
    }
}