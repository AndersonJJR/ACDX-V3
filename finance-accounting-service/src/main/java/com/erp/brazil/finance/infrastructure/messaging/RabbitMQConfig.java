package com.erp.brazil.finance.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FINANCE_EXCHANGE = "finance.exchange";
    public static final String JOURNAL_ENTRY_POSTED_QUEUE = "finance.journal.entry.posted";
    public static final String JOURNAL_ENTRY_REVERSED_QUEUE = "finance.journal.entry.reversed";
    public static final String FISCAL_PERIOD_LOCKED_QUEUE = "finance.fiscal.period.locked";
    public static final String ASSET_DEPRECIATION_POSTED_QUEUE = "finance.asset.depreciation.posted";
    public static final String PAYMENT_PROCESSED_QUEUE = "finance.payment.processed";

    @Bean
    public TopicExchange financeExchange() {
        return new TopicExchange(FINANCE_EXCHANGE, true, false);
    }

    @Bean
    public Queue journalEntryPostedQueue() {
        return QueueBuilder.durable(JOURNAL_ENTRY_POSTED_QUEUE).build();
    }

    @Bean
    public Queue journalEntryReversedQueue() {
        return QueueBuilder.durable(JOURNAL_ENTRY_REVERSED_QUEUE).build();
    }

    @Bean
    public Queue fiscalPeriodLockedQueue() {
        return QueueBuilder.durable(FISCAL_PERIOD_LOCKED_QUEUE).build();
    }

    @Bean
    public Queue assetDepreciationPostedQueue() {
        return QueueBuilder.durable(ASSET_DEPRECIATION_POSTED_QUEUE).build();
    }

    @Bean
    public Queue paymentProcessedQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESSED_QUEUE).build();
    }

    @Bean
    public Binding journalEntryPostedBinding() {
        return BindingBuilder.bind(journalEntryPostedQueue())
                .to(financeExchange())
                .with("journal.entry.posted");
    }

    @Bean
    public Binding journalEntryReversedBinding() {
        return BindingBuilder.bind(journalEntryReversedQueue())
                .to(financeExchange())
                .with("journal.entry.reversed");
    }

    @Bean
    public Binding fiscalPeriodLockedBinding() {
        return BindingBuilder.bind(fiscalPeriodLockedQueue())
                .to(financeExchange())
                .with("fiscal.period.locked");
    }

    @Bean
    public Binding assetDepreciationPostedBinding() {
        return BindingBuilder.bind(assetDepreciationPostedQueue())
                .to(financeExchange())
                .with("asset.depreciation.posted");
    }

    @Bean
    public Binding paymentProcessedBinding() {
        return BindingBuilder.bind(paymentProcessedQueue())
                .to(financeExchange())
                .with("payment.processed");
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