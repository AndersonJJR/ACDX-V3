package com.erp.brazil.sales.infrastructure.config;

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

    @Value("${rabbitmq.exchange.sales:amq.topic}")
    private String exchangeName;

    @Value("${rabbitmq.queue.stock-reserved:sales.stock.reserved}")
    private String stockReservedQueue;

    @Value("${rabbitmq.queue.stock-unreserved:sales.stock.unreserved}")
    private String stockUnreservedQueue;

    @Value("${rabbitmq.queue.nfe-emitted:sales.nfe.emitted}")
    private String nfeEmittedQueue;

    @Bean
    public TopicExchange salesExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue stockReservedQueue() {
        return new Queue(stockReservedQueue, true);
    }

    @Bean
    public Queue stockUnreservedQueue() {
        return new Queue(stockUnreservedQueue, true);
    }

    @Bean
    public Queue nfeEmittedQueue() {
        return new Queue(nfeEmittedQueue, true);
    }

    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder.bind(stockReservedQueue()).to(salesExchange()).with("sales.stock.reserved");
    }

    @Bean
    public Binding stockUnreservedBinding() {
        return BindingBuilder.bind(stockUnreservedQueue()).to(salesExchange()).with("sales.stock.unreserved");
    }

    @Bean
    public Binding nfeEmittedBinding() {
        return BindingBuilder.bind(nfeEmittedQueue()).to(salesExchange()).with("sales.nfe.emitted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}