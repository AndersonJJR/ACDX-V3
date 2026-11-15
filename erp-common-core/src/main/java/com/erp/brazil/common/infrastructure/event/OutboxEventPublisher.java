package com.erp.brazil.common.infrastructure.event;

import com.erp.brazil.common.domain.event.DomainEvent;
import com.erp.brazil.common.domain.event.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Outbox event publisher using the Transactional Outbox pattern.
 * Reads unpublished events from the outbox table and publishes them to RabbitMQ.
 * Runs on a schedule to ensure eventual consistency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.outbox.exchange:erp.events}")
    private String exchangeName;

    /**
     * Saves a domain event to the outbox table within the current transaction.
     * The event will be published asynchronously by the scheduled publisher.
     *
     * @param event the domain event to save
     */
    @Transactional
    public void saveToOutbox(DomainEvent event) {
        OutboxEvent outboxEvent = OutboxEvent.create(event.getTenantId(), event);
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEvent.setPayload(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for outbox: {}", event.getEventType(), e);
            throw new OutboxException("Failed to serialize event: " + event.getEventType(), e);
        }
        entityManager.persist(outboxEvent);
        log.debug("Saved event to outbox: {}", event.getEventId());
    }

    /**
     * Saves multiple domain events to the outbox table.
     *
     * @param events the domain events to save
     */
    @Transactional
    public void saveAllToOutbox(Iterable<DomainEvent> events) {
        for (DomainEvent event : events) {
            saveToOutbox(event);
        }
    }

    /**
     * Scheduled task to publish pending outbox events.
     * Runs every 5 seconds by default.
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = findPendingEvents();
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Publishing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                publishOutboxEvent(outboxEvent);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", outboxEvent.getId(), e);
                outboxEvent.markFailed(e.getMessage());
                entityManager.merge(outboxEvent);
            }
        }
    }

    /**
     * Finds pending outbox events ordered by creation time.
     *
     * @return list of pending events
     */
    private List<OutboxEvent> findPendingEvents() {
        TypedQuery<OutboxEvent> query = entityManager.createQuery(
                "SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC",
                OutboxEvent.class);
        query.setParameter("status", OutboxEvent.OutboxStatus.PENDING);
        query.setMaxResults(batchSize);
        return query.getResultList();
    }

    /**
     * Publishes a single outbox event.
     *
     * @param outboxEvent the outbox event
     */
    private void publishOutboxEvent(OutboxEvent outboxEvent) {
        String routingKey = buildRoutingKey(outboxEvent);
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setMessageId(outboxEvent.getId().toString());
        properties.setHeader("eventType", outboxEvent.getEventType());
        properties.setHeader("aggregateType", outboxEvent.getAggregateType());
        properties.setHeader("aggregateId", outboxEvent.getAggregateId());
        properties.setHeader("tenantId", outboxEvent.getTenantId());

        Message message = new Message(outboxEvent.getPayload().getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(exchangeName, routingKey, message);

        outboxEvent.markPublished();
        entityManager.merge(outboxEvent);

        log.debug("Published outbox event: {}", outboxEvent.getId());
    }

    /**
     * Builds the routing key from outbox event.
     */
    private String buildRoutingKey(OutboxEvent event) {
        String tenantPart = event.getTenantId() != null ? event.getTenantId() : "global";
        return String.format("erp.%s.%s.%s",
                tenantPart,
                event.getAggregateType().toLowerCase(),
                event.getEventType().toLowerCase());
    }

    /**
     * Republishes failed events (dead letter queue processing).
     * Can be triggered manually or via scheduled task.
     */
    @Transactional
    public void republishFailedEvents() {
        TypedQuery<OutboxEvent> query = entityManager.createQuery(
                "SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC",
                OutboxEvent.class);
        query.setParameter("status", OutboxEvent.OutboxStatus.FAILED);
        query.setMaxResults(batchSize);

        List<OutboxEvent> failedEvents = query.getResultList();
        for (OutboxEvent event : failedEvents) {
            event.setStatus(OutboxEvent.OutboxStatus.PENDING);
            event.setRetryCount(0);
            event.setLastError(null);
            entityManager.merge(event);
        }

        if (!failedEvents.isEmpty()) {
            log.info("Reset {} failed events for republishing", failedEvents.size());
        }
    }

    /**
     * Exception for outbox operations.
     */
    public static class OutboxException extends RuntimeException {
        public OutboxException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}