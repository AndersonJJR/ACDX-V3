package com.erp.brazil.hr.infrastructure.messaging;

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
    public static final String PAYROLL_EXCHANGE = "hr.payroll";
    public static final String NOTIFICATION_EXCHANGE = "hr.notification";
    public static final String WEBHOOK_EXCHANGE = "hr.webhook";
    public static final String AUDIT_EXCHANGE = "hr.audit";

    // Queue names
    public static final String PAYROLL_PROCESSING_QUEUE = "hr.payroll.processing";
    public static final String PAYSLIP_GENERATION_QUEUE = "hr.payslip.generation";
    public static final String NOTIFICATION_EMAIL_QUEUE = "hr.notification.email";
    public static final String NOTIFICATION_WHATSAPP_QUEUE = "hr.notification.whatsapp";
    public static final String NOTIFICATION_PUSH_QUEUE = "hr.notification.push";
    public static final String WEBHOOK_DELIVERY_QUEUE = "hr.webhook.delivery";
    public static final String WEBHOOK_RETRY_QUEUE = "hr.webhook.retry";
    public static final String WEBHOOK_DLQ_QUEUE = "hr.webhook.dlq";
    public static final String AUDIT_LOG_QUEUE = "hr.audit.log";

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

    // Payroll Exchange and Queues
    @Bean
    public TopicExchange payrollExchange() {
        return new TopicExchange(PAYROLL_EXCHANGE, true, false);
    }

    @Bean
    public Queue payrollProcessingQueue() {
        return QueueBuilder.durable(PAYROLL_PROCESSING_QUEUE).build();
    }

    @Bean
    public Binding payrollProcessingBinding() {
        return BindingBuilder.bind(payrollProcessingQueue()).to(payrollExchange()).with("payroll.process");
    }

    @Bean
    public Queue payslipGenerationQueue() {
        return QueueBuilder.durable(PAYSLIP_GENERATION_QUEUE).build();
    }

    @Bean
    public Binding payslipGenerationBinding() {
        return BindingBuilder.bind(payslipGenerationQueue()).to(payrollExchange()).with("payslip.generate");
    }

    // Notification Exchange and Queues
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationEmailQueue() {
        return QueueBuilder.durable(NOTIFICATION_EMAIL_QUEUE).build();
    }

    @Bean
    public Binding notificationEmailBinding() {
        return BindingBuilder.bind(notificationEmailQueue()).to(notificationExchange()).with("notification.email");
    }

    @Bean
    public Queue notificationWhatsAppQueue() {
        return QueueBuilder.durable(NOTIFICATION_WHATSAPP_QUEUE).build();
    }

    @Bean
    public Binding notificationWhatsAppBinding() {
        return BindingBuilder.bind(notificationWhatsAppQueue()).to(notificationExchange()).with("notification.whatsapp");
    }

    @Bean
    public Queue notificationPushQueue() {
        return QueueBuilder.durable(NOTIFICATION_PUSH_QUEUE).build();
    }

    @Bean
    public Binding notificationPushBinding() {
        return BindingBuilder.bind(notificationPushQueue()).to(notificationExchange()).with("notification.push");
    }

    // Webhook Exchange and Queues
    @Bean
    public TopicExchange webhookExchange() {
        return new TopicExchange(WEBHOOK_EXCHANGE, true, false);
    }

    @Bean
    public Queue webhookDeliveryQueue() {
        return QueueBuilder.durable(WEBHOOK_DELIVERY_QUEUE).build();
    }

    @Bean
    public Binding webhookDeliveryBinding() {
        return BindingBuilder.bind(webhookDeliveryQueue()).to(webhookExchange()).with("webhook.deliver");
    }

    @Bean
    public Queue webhookRetryQueue() {
        return QueueBuilder.durable(WEBHOOK_RETRY_QUEUE)
            .withArgument("x-dead-letter-exchange", WEBHOOK_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "webhook.deliver")
            .withArgument("x-message-ttl", 60000) // 1 minute retry delay base
            .build();
    }

    @Bean
    public Binding webhookRetryBinding() {
        return BindingBuilder.bind(webhookRetryQueue()).to(webhookExchange()).with("webhook.retry");
    }

    @Bean
    public Queue webhookDlqQueue() {
        return QueueBuilder.durable(WEBHOOK_DLQ_QUEUE).build();
    }

    @Bean
    public Binding webhookDlqBinding() {
        return BindingBuilder.bind(webhookDlqQueue()).to(webhookExchange()).with("webhook.dlq");
    }

    // Audit Exchange and Queue
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE, true, false);
    }

    @Bean
    public Queue auditLogQueue() {
        return QueueBuilder.durable(AUDIT_LOG_QUEUE).build();
    }

    @Bean
    public Binding auditLogBinding() {
        return BindingBuilder.bind(auditLogQueue()).to(auditExchange()).with("audit.log");
    }
}