package com.erp.brazil.fiscal.infrastructure.messaging;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Event publisher for domain events using RabbitMQ.
 * Publishes to topic exchange with routing keys.
 */
@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${messaging.exchange:fiscal.events}")
    private String exchange;

    @Value("${messaging.routing-key.prefix:fiscal}")
    private String routingKeyPrefix;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(EventDTO.NFEEmittedEvent event) {
        String routingKey = routingKeyPrefix + ".nfe.emitted";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.NFECancelledEvent event) {
        String routingKey = routingKeyPrefix + ".nfe.cancelled";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.NFEInutilizedEvent event) {
        String routingKey = routingKeyPrefix + ".nfe.inutilized";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.SPEDGeneratedEvent event) {
        String routingKey = routingKeyPrefix + ".sped.generated";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.TaxCalculatedEvent event) {
        String routingKey = routingKeyPrefix + ".tax.calculated";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.RetencaoCalculatedEvent event) {
        String routingKey = routingKeyPrefix + ".retencao.calculated";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.CertificadoExpiryAlertEvent event) {
        String routingKey = routingKeyPrefix + ".certificado.expiry";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(EventDTO.LeisSyncCompletedEvent event) {
        String routingKey = routingKeyPrefix + ".leis.synced";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void publish(Object event) {
        // Generic publish for unknown event types
        String routingKey = routingKeyPrefix + ".unknown";
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}