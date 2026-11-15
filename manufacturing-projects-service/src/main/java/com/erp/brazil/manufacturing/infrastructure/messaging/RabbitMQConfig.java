package com.erp.brazil.manufacturing.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String MANUFACTURING_EXCHANGE = "erp.manufacturing";
    public static final String EVENTS_EXCHANGE = "erp.events";

    // Queue names
    public static final String PRODUCTION_ORDER_RELEASED_QUEUE = "manufacturing.production.order.released";
    public static final String PRODUCTION_ORDER_COMPLETED_QUEUE = "manufacturing.production.order.completed";
    public static final String SUBCONTRACTING_ORDER_SENT_QUEUE = "manufacturing.subcontracting.order.sent";
    public static final String QUALITY_INSPECTION_PASSED_QUEUE = "manufacturing.quality.inspection.passed";
    public static final String QUALITY_INSPECTION_FAILED_QUEUE = "manufacturing.quality.inspection.failed";
    public static final String TIMESHEET_SUBMITTED_QUEUE = "manufacturing.timesheet.submitted";

    // Routing keys
    public static final String PRODUCTION_ORDER_RELEASED_RK = "manufacturing.production.order.released";
    public static final String PRODUCTION_ORDER_COMPLETED_RK = "manufacturing.production.order.completed";
    public static final String SUBCONTRACTING_ORDER_SENT_RK = "manufacturing.subcontracting.order.sent";
    public static final String QUALITY_INSPECTION_PASSED_RK = "manufacturing.quality.inspection.passed";
    public static final String QUALITY_INSPECTION_FAILED_RK = "manufacturing.quality.inspection.failed";
    public static final String TIMESHEET_SUBMITTED_RK = "manufacturing.timesheet.submitted";

    @Bean
    public TopicExchange manufacturingExchange() {
        return new TopicExchange(MANUFACTURING_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    // Queues
    @Bean
    public Queue productionOrderReleasedQueue() {
        return QueueBuilder.durable(PRODUCTION_ORDER_RELEASED_QUEUE).build();
    }

    @Bean
    public Queue productionOrderCompletedQueue() {
        return QueueBuilder.durable(PRODUCTION_ORDER_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue subcontractingOrderSentQueue() {
        return QueueBuilder.durable(SUBCONTRACTING_ORDER_SENT_QUEUE).build();
    }

    @Bean
    public Queue qualityInspectionPassedQueue() {
        return QueueBuilder.durable(QUALITY_INSPECTION_PASSED_QUEUE).build();
    }

    @Bean
    public Queue qualityInspectionFailedQueue() {
        return QueueBuilder.durable(QUALITY_INSPECTION_FAILED_QUEUE).build();
    }

    @Bean
    public Queue timesheetSubmittedQueue() {
        return QueueBuilder.durable(TIMESHEET_SUBMITTED_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding productionOrderReleasedBinding() {
        return BindingBuilder.bind(productionOrderReleasedQueue())
            .to(manufacturingExchange())
            .with(PRODUCTION_ORDER_RELEASED_RK);
    }

    @Bean
    public Binding productionOrderCompletedBinding() {
        return BindingBuilder.bind(productionOrderCompletedQueue())
            .to(manufacturingExchange())
            .with(PRODUCTION_ORDER_COMPLETED_RK);
    }

    @Bean
    public Binding subcontractingOrderSentBinding() {
        return BindingBuilder.bind(subcontractingOrderSentQueue())
            .to(manufacturingExchange())
            .with(SUBCONTRACTING_ORDER_SENT_RK);
    }

    @Bean
    public Binding qualityInspectionPassedBinding() {
        return BindingBuilder.bind(qualityInspectionPassedQueue())
            .to(manufacturingExchange())
            .with(QUALITY_INSPECTION_PASSED_RK);
    }

    @Bean
    public Binding qualityInspectionFailedBinding() {
        return BindingBuilder.bind(qualityInspectionFailedQueue())
            .to(manufacturingExchange())
            .with(QUALITY_INSPECTION_FAILED_RK);
    }

    @Bean
    public Binding timesheetSubmittedBinding() {
        return BindingBuilder.bind(timesheetSubmittedQueue())
            .to(manufacturingExchange())
            .with(TIMESHEET_SUBMITTED_RK);
    }

    // Also bind to events exchange for cross-service communication
    @Bean
    public Binding productionOrderReleasedEventsBinding() {
        return BindingBuilder.bind(productionOrderReleasedQueue())
            .to(eventsExchange())
            .with(PRODUCTION_ORDER_RELEASED_RK);
    }

    @Bean
    public Binding productionOrderCompletedEventsBinding() {
        return BindingBuilder.bind(productionOrderCompletedQueue())
            .to(eventsExchange())
            .with(PRODUCTION_ORDER_COMPLETED_RK);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}