package com.erp.brazil.finance.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    public static final String EVENTS_EXCHANGE = "erp.events";

    // Journal Entry Events
    public static final String JOURNAL_ENTRY_POSTED_QUEUE = "erp.finance.journal-entry-posted";
    public static final String JOURNAL_ENTRY_POSTED_ROUTING_KEY = "journal.entry.posted";

    // Payment Events
    public static final String PAYMENT_PROCESSED_QUEUE = "erp.finance.payment-processed";
    public static final String PAYMENT_PROCESSED_ROUTING_KEY = "payment.processed";

    // Fiscal Period Events
    public static final String FISCAL_PERIOD_LOCKED_QUEUE = "erp.finance.fiscal-period-locked";
    public static final String FISCAL_PERIOD_LOCKED_ROUTING_KEY = "fiscal.period.locked";

    // Asset Events
    public static final String ASSET_DEPRECIATION_POSTED_QUEUE = "erp.finance.asset-depreciation-posted";
    public static final String ASSET_DEPRECIATION_POSTED_ROUTING_KEY = "asset.depreciation.posted";

    // Inbound Events (from other services)
    public static final String INVOICE_VALIDATED_QUEUE = "erp.finance.invoice-validated";
    public static final String INVOICE_VALIDATED_ROUTING_KEY = "invoice.validated";

    public static final String GOODS_RECEIVED_QUEUE = "erp.finance.goods-received";
    public static final String GOODS_RECEIVED_ROUTING_KEY = "goods.received";

    public static final String PURCHASE_ORDER_APPROVED_QUEUE = "erp.finance.purchase-order-approved";
    public static final String PURCHASE_ORDER_APPROVED_ROUTING_KEY = "purchase.order.approved";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    // Outbound queues
    @Bean
    public Queue journalEntryPostedQueue() {
        return QueueBuilder.durable(JOURNAL_ENTRY_POSTED_QUEUE).build();
    }

    @Bean
    public Binding journalEntryPostedBinding() {
        return BindingBuilder.bind(journalEntryPostedQueue())
                .to(eventsExchange())
                .with(JOURNAL_ENTRY_POSTED_ROUTING_KEY);
    }

    @Bean
    public Queue paymentProcessedQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESSED_QUEUE).build();
    }

    @Bean
    public Binding paymentProcessedBinding() {
        return BindingBuilder.bind(paymentProcessedQueue())
                .to(eventsExchange())
                .with(PAYMENT_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public Queue fiscalPeriodLockedQueue() {
        return QueueBuilder.durable(FISCAL_PERIOD_LOCKED_QUEUE).build();
    }

    @Bean
    public Binding fiscalPeriodLockedBinding() {
        return BindingBuilder.bind(fiscalPeriodLockedQueue())
                .to(eventsExchange())
                .with(FISCAL_PERIOD_LOCKED_ROUTING_KEY);
    }

    @Bean
    public Queue assetDepreciationPostedQueue() {
        return QueueBuilder.durable(ASSET_DEPRECIATION_POSTED_QUEUE).build();
    }

    @Bean
    public Binding assetDepreciationPostedBinding() {
        return BindingBuilder.bind(assetDepreciationPostedQueue())
                .to(eventsExchange())
                .with(ASSET_DEPRECIATION_POSTED_ROUTING_KEY);
    }

    // Inbound queues
    @Bean
    public Queue invoiceValidatedQueue() {
        return QueueBuilder.durable(INVOICE_VALIDATED_QUEUE).build();
    }

    @Bean
    public Binding invoiceValidatedBinding() {
        return BindingBuilder.bind(invoiceValidatedQueue())
                .to(eventsExchange())
                .with(INVOICE_VALIDATED_ROUTING_KEY);
    }

    @Bean
    public Queue goodsReceivedQueue() {
        return QueueBuilder.durable(GOODS_RECEIVED_QUEUE).build();
    }

    @Bean
    public Binding goodsReceivedBinding() {
        return BindingBuilder.bind(goodsReceivedQueue())
                .to(eventsExchange())
                .with(GOODS_RECEIVED_ROUTING_KEY);
    }

    @Bean
    public Queue purchaseOrderApprovedQueue() {
        return QueueBuilder.durable(PURCHASE_ORDER_APPROVED_QUEUE).build();
    }

    @Bean
    public Binding purchaseOrderApprovedBinding() {
        return BindingBuilder.bind(purchaseOrderApprovedQueue())
                .to(eventsExchange())
                .with(PURCHASE_ORDER_APPROVED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}