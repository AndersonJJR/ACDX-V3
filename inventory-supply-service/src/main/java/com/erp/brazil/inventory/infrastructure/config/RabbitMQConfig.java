package com.erp.brazil.inventory.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.stock-reserved:stock.reserved}")
    private String stockReservedKey;

    @Value("${app.messaging.routing-key.stock-unreserved:stock.unreserved}")
    private String stockUnreservedKey;

    @Value("${app.messaging.routing-key.goods-received:goods.received}")
    private String goodsReceivedKey;

    @Value("${app.messaging.routing-key.invoice-matched:invoice.matched}")
    private String invoiceMatchedKey;

    @Value("${app.messaging.routing-key.invoice-mismatch:invoice.mismatch}")
    private String invoiceMismatchKey;

    @Value("${app.messaging.routing-key.valuation-recalculated:valuation.recalculated}")
    private String valuationRecalculatedKey;

    @Value("${app.messaging.routing-key.stock-transferred:stock.transferred}")
    private String stockTransferredKey;

    @Value("${app.messaging.queue.reposting:inventory.reposting.trigger}")
    private String repostingQueue;

    @Value("${app.messaging.queue.three-way-match:finance.purchase_invoice.created}")
    private String threeWayMatchQueue;

    @Value("${app.messaging.queue.sales-order-confirmed:sales.order.confirmed}")
    private String salesOrderConfirmedQueue;

    @Value("${app.messaging.queue.sales-order-cancelled:sales.order.cancelled}")
    private String salesOrderCancelledQueue;

    @Value("${app.messaging.queue.production-order-completed:production.order.completed}")
    private String productionOrderCompletedQueue;

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(inventoryExchange, true, false);
    }

    // Outbound queues (published by this service)
    @Bean
    public Queue stockReservedQueue() {
        return QueueBuilder.durable(inventoryExchange + ".stock.reserved")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "stock.reserved.dlq")
                .build();
    }

    @Bean
    public Queue stockUnreservedQueue() {
        return QueueBuilder.durable(inventoryExchange + ".stock.unreserved")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "stock.unreserved.dlq")
                .build();
    }

    @Bean
    public Queue goodsReceivedQueue() {
        return QueueBuilder.durable(inventoryExchange + ".goods.received")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "goods.received.dlq")
                .build();
    }

    @Bean
    public Queue invoiceMatchedQueue() {
        return QueueBuilder.durable(inventoryExchange + ".invoice.matched")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "invoice.matched.dlq")
                .build();
    }

    @Bean
    public Queue invoiceMismatchQueue() {
        return QueueBuilder.durable(inventoryExchange + ".invoice.mismatch")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "invoice.mismatch.dlq")
                .build();
    }

    @Bean
    public Queue valuationRecalculatedQueue() {
        return QueueBuilder.durable(inventoryExchange + ".valuation.recalculated")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "valuation.recalculated.dlq")
                .build();
    }

    @Bean
    public Queue stockTransferredQueue() {
        return QueueBuilder.durable(inventoryExchange + ".stock.transferred")
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "stock.transferred.dlq")
                .build();
    }

    // Inbound queues (consumed by this service)
    @Bean
    public Queue repostingQueue() {
        return QueueBuilder.durable(repostingQueue)
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "reposting.dlq")
                .build();
    }

    @Bean
    public Queue threeWayMatchQueue() {
        return QueueBuilder.durable(threeWayMatchQueue)
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "three-way-match.dlq")
                .build();
    }

    @Bean
    public Queue salesOrderConfirmedQueue() {
        return QueueBuilder.durable(salesOrderConfirmedQueue)
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "sales-order-confirmed.dlq")
                .build();
    }

    @Bean
    public Queue salesOrderCancelledQueue() {
        return QueueBuilder.durable(salesOrderCancelledQueue)
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "sales-order-cancelled.dlq")
                .build();
    }

    @Bean
    public Queue productionOrderCompletedQueue() {
        return QueueBuilder.durable(productionOrderCompletedQueue)
                .withArgument("x-dead-letter-exchange", inventoryExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "production-order-completed.dlq")
                .build();
    }

    // Bindings for outbound messages
    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder.bind(stockReservedQueue()).to(inventoryExchange()).with(stockReservedKey);
    }

    @Bean
    public Binding stockUnreservedBinding() {
        return BindingBuilder.bind(stockUnreservedQueue()).to(inventoryExchange()).with(stockUnreservedKey);
    }

    @Bean
    public Binding goodsReceivedBinding() {
        return BindingBuilder.bind(goodsReceivedQueue()).to(inventoryExchange()).with(goodsReceivedKey);
    }

    @Bean
    public Binding invoiceMatchedBinding() {
        return BindingBuilder.bind(invoiceMatchedQueue()).to(inventoryExchange()).with(invoiceMatchedKey);
    }

    @Bean
    public Binding invoiceMismatchBinding() {
        return BindingBuilder.bind(invoiceMismatchQueue()).to(inventoryExchange()).with(invoiceMismatchKey);
    }

    @Bean
    public Binding valuationRecalculatedBinding() {
        return BindingBuilder.bind(valuationRecalculatedQueue()).to(inventoryExchange()).with(valuationRecalculatedKey);
    }

    @Bean
    public Binding stockTransferredBinding() {
        return BindingBuilder.bind(stockTransferredQueue()).to(inventoryExchange()).with(stockTransferredKey);
    }

    // Bindings for inbound messages
    @Bean
    public Binding repostingBinding() {
        return BindingBuilder.bind(repostingQueue()).to(inventoryExchange()).with("inventory.reposting.trigger");
    }

    @Bean
    public Binding threeWayMatchBinding() {
        return BindingBuilder.bind(threeWayMatchQueue()).to(inventoryExchange()).with("finance.purchase_invoice.created");
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

    // Dead Letter Exchange
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(inventoryExchange + ".dlx", true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(inventoryExchange + ".dlq").build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("#");
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