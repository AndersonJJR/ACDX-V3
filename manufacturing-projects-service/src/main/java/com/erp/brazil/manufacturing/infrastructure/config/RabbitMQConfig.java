package com.erp.brazil.manufacturing.infrastructure.config;

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

    @Value("${app.rabbitmq.exchange:manufacturing.events}")
    private String exchange;

    @Value("${app.rabbitmq.queue.sales-order-confirmed:manufacturing.sales-order-confirmed}")
    private String salesOrderConfirmedQueue;

    @Value("${app.rabbitmq.queue.sales-order-cancelled:manufacturing.sales-order-cancelled}")
    private String salesOrderCancelledQueue;

    @Value("${app.rabbitmq.queue.goods-received:manufacturing.goods-received}")
    private String goodsReceivedQueue;

    @Value("${app.rabbitmq.queue.material-reserved:manufacturing.material-reserved}")
    private String materialReservedQueue;

    @Value("${app.rabbitmq.queue.material-released:manufacturing.material-released}")
    private String materialReleasedQueue;

    @Bean
    public TopicExchange manufacturingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue salesOrderConfirmedQueue() {
        return QueueBuilder.durable(salesOrderConfirmedQueue).build();
    }

    @Bean
    public Queue salesOrderCancelledQueue() {
        return QueueBuilder.durable(salesOrderCancelledQueue).build();
    }

    @Bean
    public Queue goodsReceivedQueue() {
        return QueueBuilder.durable(goodsReceivedQueue).build();
    }

    @Bean
    public Queue materialReservedQueue() {
        return QueueBuilder.durable(materialReservedQueue).build();
    }

    @Bean
    public Queue materialReleasedQueue() {
        return QueueBuilder.durable(materialReleasedQueue).build();
    }

    @Bean
    public Binding salesOrderConfirmedBinding() {
        return BindingBuilder.bind(salesOrderConfirmedQueue())
                .to(manufacturingExchange())
                .with("sales.order.confirmed");
    }

    @Bean
    public Binding salesOrderCancelledBinding() {
        return BindingBuilder.bind(salesOrderCancelledQueue())
                .to(manufacturingExchange())
                .with("sales.order.cancelled");
    }

    @Bean
    public Binding goodsReceivedBinding() {
        return BindingBuilder.bind(goodsReceivedQueue())
                .to(manufacturingExchange())
                .with("inventory.goods.received");
    }

    @Bean
    public Binding materialReservedBinding() {
        return BindingBuilder.bind(materialReservedQueue())
                .to(manufacturingExchange())
                .with("inventory.material.reserved");
    }

    @Bean
    public Binding materialReleasedBinding() {
        return BindingBuilder.bind(materialReleasedQueue())
                .to(manufacturingExchange())
                .with("inventory.material.released");
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