package com.erp.brazil.sales.infrastructure.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.sales:amq.topic}")
    private String exchange;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Object event) {
        String routingKey = event.getClass().getSimpleName().replace("Event", "").toLowerCase();
        rabbitTemplate.convertAndSend(exchange, "sales." + routingKey, event);
    }
}