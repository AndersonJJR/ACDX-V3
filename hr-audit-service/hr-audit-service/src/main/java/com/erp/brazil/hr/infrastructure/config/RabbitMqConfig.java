package com.erp.brazil.hr.infrastructure.config;

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
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE = "hr.notifications";
    public static final String WEBHOOK_EXCHANGE = "hr.webhooks";
    public static final String AUDIT_EXCHANGE = "hr.audit";
    public static final String PAYROLL_EXCHANGE = "hr.payroll";

    public static final String NOTIFICATION_QUEUE = "hr.notifications.queue";
    public static final String NOTIFICATION_DLQ = "hr.notifications.dlq";
    public static final String WEBHOOK_QUEUE = "hr.webhooks.queue";
    public static final String WEBHOOK_DLQ = "hr.webhooks.dlq";
    public static final String PAYROLL_QUEUE = "hr.payroll.queue";
    public static final String PAYROLL_DLQ = "hr.payroll.dlq";

    @Value("${app.webhook.dead-letter.queue-name:hr:webhooks:dlq}")
    private String webhookDlqName;

    @Value("${app.notifications.queue.redis-key:hr:notifications:queue}")
    private String notificationQueueKey;

    // Exchanges
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange webhookExchange() {
        return new TopicExchange(WEBHOOK_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange payrollExchange() {
        return new TopicExchange(PAYROLL_EXCHANGE, true, false);
    }

    // Dead Letter Exchanges
    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_EXCHANGE + ".dlx", true, false);
    }

    @Bean
    public DirectExchange webhookDlx() {
        return new DirectExchange(WEBHOOK_EXCHANGE + ".dlx", true, false);
    }

    @Bean
    public DirectExchange payrollDlx() {
        return new DirectExchange(PAYROLL_EXCHANGE + ".dlx", true, false);
    }

    // Queues
    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_EXCHANGE + ".dlx");
        args.put("x-dead-letter-routing-key", "notification.dlq");
        return QueueBuilder.durable(NOTIFICATION_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Queue webhookQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", WEBHOOK_EXCHANGE + ".dlx");
        args.put("x-dead-letter-routing-key", "webhook.dlq");
        return QueueBuilder.durable(WEBHOOK_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue webhookDlq() {
        return QueueBuilder.durable(WEBHOOK_DLQ).build();
    }

    @Bean
    public Queue payrollQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", PAYROLL_EXCHANGE + ".dlx");
        args.put("x-dead-letter-routing-key", "payroll.dlq");
        return QueueBuilder.durable(PAYROLL_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue payrollDlq() {
        return QueueBuilder.durable(PAYROLL_DLQ).build();
    }

    // Bindings
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with("notification.*");
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq()).to(notificationDlx()).with("notification.dlq");
    }

    @Bean
    public Binding webhookBinding() {
        return BindingBuilder.bind(webhookQueue()).to(webhookExchange()).with("webhook.*");
    }

    @Bean
    public Binding webhookDlqBinding() {
        return BindingBuilder.bind(webhookDlq()).to(webhookDlx()).with("webhook.dlq");
    }

    @Bean
    public Binding payrollBinding() {
        return BindingBuilder.bind(payrollQueue()).to(payrollExchange()).with("payroll.*");
    }

    @Bean
    public Binding payrollDlqBinding() {
        return BindingBuilder.bind(payrollDlq()).to(payrollDlx()).with("payroll.dlq");
    }

    // Message Converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Rabbit Template
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        return template;
    }
}