package com.erp.brazil.manufacturing.infrastructure.messaging;

import com.erp.brazil.manufacturing.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishProductionOrderReleased(ProductionOrderReleasedEvent event) {
        log.info("Publishing ProductionOrderReleasedEvent: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.PRODUCTION_ORDER_RELEASED_RK,
            event
        );
    }

    public void publishProductionOrderCompleted(ProductionOrderCompletedEvent event) {
        log.info("Publishing ProductionOrderCompletedEvent: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.PRODUCTION_ORDER_COMPLETED_RK,
            event
        );
    }

    public void publishSubcontractingOrderSent(SubcontractingOrderSentEvent event) {
        log.info("Publishing SubcontractingOrderSentEvent: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.SUBCONTRACTING_ORDER_SENT_RK,
            event
        );
    }

    public void publishQualityInspectionPassed(QualityInspectionPassedEvent event) {
        log.info("Publishing QualityInspectionPassedEvent: {}", event.getInspectionNumber());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.QUALITY_INSPECTION_PASSED_RK,
            event
        );
    }

    public void publishQualityInspectionFailed(QualityInspectionFailedEvent event) {
        log.info("Publishing QualityInspectionFailedEvent: {}", event.getInspectionNumber());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.QUALITY_INSPECTION_FAILED_RK,
            event
        );
    }

    public void publishTimesheetSubmitted(TimesheetSubmittedEvent event) {
        log.info("Publishing TimesheetSubmittedEvent for project: {}", event.getProjectId());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MANUFACTURING_EXCHANGE,
            RabbitMQConfig.TIMESHEET_SUBMITTED_RK,
            event
        );
    }
}