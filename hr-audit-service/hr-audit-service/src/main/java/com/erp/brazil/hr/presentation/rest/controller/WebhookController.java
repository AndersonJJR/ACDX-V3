package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.WebhookDelivery;
import com.erp.brazil.hr.domain.model.WebhookDeliveryStatus;
import com.erp.brazil.hr.domain.model.WebhookSubscription;
import com.erp.brazil.hr.domain.repository.WebhookDeliveryRepository;
import com.erp.brazil.hr.domain.repository.WebhookSubscriptionRepository;
import com.erp.brazil.hr.presentation.rest.dto.WebhookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Outbound webhook management")
public class WebhookController {

    private final ProcessWebhookUseCase webhookUseCase;
    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;

    @PostMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create webhook subscription")
    public ResponseEntity<WebhookDto.SubscriptionResponse> createSubscription(@Valid @RequestBody WebhookDto.SubscriptionRequest request) {
        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setCompanyId(getCurrentCompanyId());
        subscription.setName(request.getName());
        subscription.setUrl(request.getUrl());
        subscription.setSecret(java.util.UUID.randomUUID().toString());
        subscription.setEvents(request.getEvents());
        subscription.setHeaders(request.getHeaders());
        subscription.setRetryPolicy(request.getRetryPolicy());
        subscription.setIsActive(true);

        WebhookSubscription saved = subscriptionRepository.save(subscription);
        return ResponseEntity.ok(toSubscriptionResponse(saved));
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List webhook subscriptions")
    public ResponseEntity<List<WebhookDto.SubscriptionResponse>> listSubscriptions() {
        List<WebhookSubscription> subscriptions = subscriptionRepository.findByCompanyId(getCurrentCompanyId());
        return ResponseEntity.ok(subscriptions.stream().map(this::toSubscriptionResponse).collect(Collectors.toList()));
    }

    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get webhook subscription")
    public ResponseEntity<WebhookDto.SubscriptionResponse> getSubscription(@PathVariable Long id) {
        WebhookSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        return ResponseEntity.ok(toSubscriptionResponse(subscription));
    }

    @PutMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update webhook subscription")
    public ResponseEntity<WebhookDto.SubscriptionResponse> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody WebhookDto.SubscriptionRequest request) {
        WebhookSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        subscription.setName(request.getName());
        subscription.setUrl(request.getUrl());
        subscription.setEvents(request.getEvents());
        subscription.setHeaders(request.getHeaders());
        subscription.setRetryPolicy(request.getRetryPolicy());

        WebhookSubscription saved = subscriptionRepository.save(subscription);
        return ResponseEntity.ok(toSubscriptionResponse(saved));
    }

    @DeleteMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete webhook subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Publish event to webhooks")
    public ResponseEntity<Void> publishEvent(@Valid @RequestBody WebhookDto.PublishEventRequest request) {
        webhookUseCase.publishEvent(request.getEventType(), request.getPayload(), getCurrentCompanyId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deliveries")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List webhook deliveries")
    public ResponseEntity<WebhookDto.PageResponse> listDeliveries(
            @RequestParam(required = false) Long subscriptionId,
            @RequestParam(required = false) WebhookDeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<WebhookDelivery> deliveries;
        if (subscriptionId != null && status != null) {
            deliveries = deliveryRepository.findBySubscriptionIdAndStatus(subscriptionId, status);
        } else if (subscriptionId != null) {
            deliveries = deliveryRepository.findBySubscriptionId(subscriptionId);
        } else if (status != null) {
            deliveries = deliveryRepository.findByStatus(status);
        } else {
            deliveries = deliveryRepository.findAll();
        }

        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), deliveries.size());
        List<WebhookDelivery> pageContent = deliveries.subList(start, end);

        return ResponseEntity.ok(WebhookDto.PageResponse.builder()
            .content(pageContent.stream().map(this::toDeliveryResponse).collect(Collectors.toList()))
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(deliveries.size())
            .totalPages((int) Math.ceil((double) deliveries.size() / pageable.getPageSize()))
            .first(pageable.getPageNumber() == 0)
            .last(end >= deliveries.size())
            .build());
    }

    @GetMapping("/deliveries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get webhook delivery details")
    public ResponseEntity<WebhookDto.DeliveryResponse> getDelivery(@PathVariable Long id) {
        WebhookDelivery delivery = deliveryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/deliveries/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry failed webhook delivery")
    public ResponseEntity<Void> retryDelivery(@PathVariable Long id) {
        // Would trigger retry
        return ResponseEntity.ok().build();
    }

    private WebhookDto.SubscriptionResponse toSubscriptionResponse(WebhookSubscription subscription) {
        return WebhookDto.SubscriptionResponse.builder()
            .id(subscription.getId())
            .uuid(subscription.getUuid())
            .companyId(subscription.getCompanyId())
            .name(subscription.getName())
            .url(subscription.getUrl())
            .events(subscription.getEvents())
            .headers(subscription.getHeaders())
            .isActive(subscription.getIsActive())
            .retryPolicy(subscription.getRetryPolicy())
            .lastTriggeredAt(subscription.getLastTriggeredAt())
            .consecutiveFailures(subscription.getConsecutiveFailures())
            .build();
    }

    private WebhookDto.DeliveryResponse toDeliveryResponse(WebhookDelivery delivery) {
        return WebhookDto.DeliveryResponse.builder()
            .id(delivery.getId())
            .uuid(delivery.getUuid())
            .subscriptionId(delivery.getSubscription().getId())
            .subscriptionName(delivery.getSubscription().getName())
            .eventType(delivery.getEventType())
            .payload(delivery.getPayload())
            .responseStatus(delivery.getResponseStatus())
            .responseBody(delivery.getResponseBody())
            .responseHeaders(delivery.getResponseHeaders())
            .attempt(delivery.getAttempt())
            .status(delivery.getStatus())
            .statusDescription(delivery.getStatus().getDescription())
            .errorMessage(delivery.getErrorMessage())
            .nextRetryAt(delivery.getNextRetryAt())
            .sentAt(delivery.getSentAt())
            .build();
    }

    private Long getCurrentCompanyId() {
        return 1L;
    }
}