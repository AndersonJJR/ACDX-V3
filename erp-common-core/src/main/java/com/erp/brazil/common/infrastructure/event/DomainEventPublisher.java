package com.erp.brazil.common.infrastructure.event;

import com.erp.brazil.common.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Domain event publisher using RabbitMQ.
 * Publishes domain events to topic exchanges for consumption by other services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.events.exchange:erp.events}")
    private String exchangeName;

    @Value("${app.events.routing-key-prefix:erp}")
    private String routingKeyPrefix;

    /**
     * Publishes a domain event to RabbitMQ.
     *
     * @param event the domain event to publish
     */
    public void publish(DomainEvent event) {
        try {
            String routingKey = buildRoutingKey(event);
            String payload = objectMapper.writeValueAsString(event);

            Message message = buildMessage(payload, event);
            rabbitTemplate.send(exchangeName, routingKey, message);

            log.debug("Published event: {} with routing key: {}", event.getEventType(), routingKey);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new EventPublishingException("Failed to serialize event: " + event.getEventType(), e);
        } catch (AmqpException e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
            throw new EventPublishingException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Publishes a domain event with custom routing key.
     *
     * @param event the domain event
     * @param customRoutingKey the custom routing key
     */
    public void publish(DomainEvent event, String customRoutingKey) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Message message = buildMessage(payload, event);
            rabbitTemplate.send(exchangeName, customRoutingKey, message);

            log.debug("Published event: {} with custom routing key: {}", event.getEventType(), customRoutingKey);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new EventPublishingException("Failed to serialize event: " + event.getEventType(), e);
        } catch (AmqpException e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
            throw new EventPublishingException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Publishes multiple events in a batch.
     *
     * @param events the events to publish
     */
    public void publishBatch(Iterable<DomainEvent> events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }

    /**
     * Builds the routing key for the event.
     * Format: {prefix}.{tenantId}.{aggregateType}.{eventType}
     */
    private String buildRoutingKey(DomainEvent event) {
        String tenantPart = event.getTenantId() != null ? event.getTenantId() : "global";
        return String.format("%s.%s.%s.%s",
                routingKeyPrefix,
                tenantPart,
                event.getAggregateType().toLowerCase(),
                event.getEventType().toLowerCase());
    }

    /**
     * Builds the RabbitMQ message with headers.
     */
    private Message buildMessage(String payload, DomainEvent event) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setMessageId(event.getEventId());
        properties.setCorrelationId(event.getCorrelationId());
        properties.setHeader("eventType", event.getEventType());
        properties.setHeader("aggregateType", event.getAggregateType());
        properties.setHeader("aggregateId", event.getAggregateId());
        properties.setHeader("tenantId", event.getTenantId());
        properties.setHeader("userId", event.getUserId());
        properties.setHeader("companyId", event.getCompanyId());
        properties.setHeader("version", event.getVersion());
        properties.setTimestamp(Date.from(event.getOccurredAt().toInstant()));

        return new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
    }

    /**
     * Exception for event publishing failures.
     */
    public static class EventPublishingException extends RuntimeException {
        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}