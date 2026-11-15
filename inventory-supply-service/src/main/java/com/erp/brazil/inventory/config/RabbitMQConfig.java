package com.erp.brazil.inventory.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.inventory:inventory.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.reposting:inventory.reposting.queue}")
    private String repostingQueue;

    @Value("${app.rabbitmq.queue.three-way-match:inventory.three-way-match.queue}")
    private String threeWayMatchQueue;

    @Value("${app.rabbitmq.queue.sales-order-confirmed:inventory.sales-order-confirmed.queue}")
    private String salesOrderConfirmedQueue;

    @Value("${app.rabbitmq.queue.sales-order-cancelled:inventory.sales-order-cancelled.queue}")
    private String salesOrderCancelledQueue;

    @Value("${app.rabbitmq.queue.production-order-completed:inventory.production-order-completed.queue}")
    private String productionOrderCompletedQueue;

    @Value("${app.rabbitmq.routing-key.invoice-matched:invoice.matched}")
    private String invoiceMatchedRoutingKey;

    @Value("${app.rabbitmq.routing-key.invoice-mismatch:invoice.mismatch}")
    private String invoiceMismatchRoutingKey;

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue repostingQueue() {
        return QueueBuilder.durable(repostingQueue)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "reposting.dlq")
                .build();
    }

    @Bean
    public Queue threeWayMatchQueue() {
        return QueueBuilder.durable(threeWayMatchQueue)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "three-way-match.dlq")
                .build();
    }

    @Bean
    public Queue salesOrderConfirmedQueue() {
        return QueueBuilder.durable(salesOrderConfirmedQueue)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "sales-order-confirmed.dlq")
                .build();
    }

    @Bean
    public Queue salesOrderCancelledQueue() {
        return QueueBuilder.durable(salesOrderCancelledQueue)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "sales-order-cancelled.dlq")
                .build();
    }

    @Bean
    public Queue productionOrderCompletedQueue() {
        return QueueBuilder.durable(productionOrderCompletedQueue)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", "production-order-completed.dlq")
                .build();
    }

    @Bean
    public Binding repostingBinding() {
        return BindingBuilder.bind(repostingQueue()).to(inventoryExchange()).with("reposting.execute");
    }

    @Bean
    public Binding threeWayMatchBinding() {
        return BindingBuilder.bind(threeWayMatchQueue()).to(inventoryExchange()).with("invoice.process");
    }

    @Bean
    public Binding salesOrderConfirmedBinding() {
        return BindingBuilder.bind(salesOrderConfirmedQueue()).to(inventoryExchange()).with("sales.order.confirmed");
    }

    @Bean
    public Binding salesOrderCancelledBinding() {
        return BindingBuilder.bind(salesOrderCancelledQueue()).to(inventoryExchange()).with("sales.order.cancelled");
    }

    @Bean
    public Binding productionOrderCompletedBinding() {
        return BindingBuilder.bind(productionOrderCompletedQueue()).to(inventoryExchange()).with("production.order.completed");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        return template;
    }
}