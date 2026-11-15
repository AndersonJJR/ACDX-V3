package com.erp.brazil.manufacturing.application.event;

import com.erp.brazil.manufacturing.domain.event.*;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrderItem;
import com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt;
import com.erp.brazil.manufacturing.domain.model.QualityInspection;
import com.erp.brazil.manufacturing.domain.model.Timesheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:manufacturing.events}")
    private String exchange;

    public void publishProductionOrderCreated(ProductionOrder productionOrder) {
        ProductionOrderCreatedEvent event = ProductionOrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .productionOrderId(productionOrder.getId())
                .orderNumber(productionOrder.getOrderNumber())
                .productId(productionOrder.getProductId())
                .orderedQuantity(productionOrder.getOrderedQuantity())
                .orderType(productionOrder.getOrderType().name())
                .salesOrderId(productionOrder.getSalesOrderId())
                .projectId(productionOrder.getProjectId())
                .build();

        rabbitTemplate.convertAndSend(exchange, "production.order.created", event);
        log.debug("Published ProductionOrderCreatedEvent for order {}", productionOrder.getOrderNumber());
    }

    public void publishProductionOrderReleased(ProductionOrder productionOrder) {
        List<ProductionOrderReleasedEvent.MaterialReservationItem> materials = productionOrder.getItems().stream()
                .filter(item -> item.getRequiredQuantity().compareTo(item.getIssuedQuantity()) > 0)
                .map(item -> ProductionOrderReleasedEvent.MaterialReservationItem.builder()
                        .componentId(item.getComponentId())
                        .requiredQuantity(item.getRequiredQuantity().subtract(item.getIssuedQuantity()))
                        .unitOfMeasure(item.getUnitOfMeasure())
                        .warehouseId(item.getWarehouseId())
                        .build())
                .collect(Collectors.toList());

        ProductionOrderReleasedEvent event = ProductionOrderReleasedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .productionOrderId(productionOrder.getId())
                .orderNumber(productionOrder.getOrderNumber())
                .productId(productionOrder.getProductId())
                .orderedQuantity(productionOrder.getOrderedQuantity())
                .materialReservations(materials)
                .build();

        rabbitTemplate.convertAndSend(exchange, "production.order.released", event);
        log.debug("Published ProductionOrderReleasedEvent for order {}", productionOrder.getOrderNumber());
    }

    public void publishProductionReported(ProductionOrder productionOrder, BigDecimal producedQuantity, BigDecimal scrappedQuantity) {
        ProductionReportedEvent event = ProductionReportedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .productionOrderId(productionOrder.getId())
                .orderNumber(productionOrder.getOrderNumber())
                .productId(productionOrder.getProductId())
                .producedQuantity(producedQuantity)
                .scrappedQuantity(scrappedQuantity)
                .warehouseId(productionOrder.getWarehouseId())
                .build();

        rabbitTemplate.convertAndSend(exchange, "production.reported", event);
        log.debug("Published ProductionReportedEvent for order {}", productionOrder.getOrderNumber());
    }

    public void publishProductionOrderCompleted(ProductionOrder productionOrder) {
        ProductionOrderCompletedEvent event = ProductionOrderCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .productionOrderId(productionOrder.getId())
                .orderNumber(productionOrder.getOrderNumber())
                .productId(productionOrder.getProductId())
                .producedQuantity(productionOrder.getProducedQuantity())
                .scrappedQuantity(productionOrder.getScrappedQuantity())
                .warehouseId(productionOrder.getWarehouseId())
                .build();

        rabbitTemplate.convertAndSend(exchange, "production.order.completed", event);
        log.debug("Published ProductionOrderCompletedEvent for order {}", productionOrder.getOrderNumber());
    }

    public void publishOperationStarted(ProductionOrder productionOrder, ProductionOrderOperation operation, UUID employeeId) {
        // Could publish operation started event if needed
        log.debug("Operation {} started for production order {}", operation.getId(), productionOrder.getOrderNumber());
    }

    public void publishOperationCompleted(ProductionOrder productionOrder, ProductionOrderOperation operation, UUID employeeId) {
        // Could publish operation completed event if needed
        log.debug("Operation {} completed for production order {}", operation.getId(), productionOrder.getOrderNumber());
    }

    public void publishSubcontractingOrderCreated(SubcontractingOrder subcontractingOrder) {
        List<SubcontractingOrderCreatedEvent.SubcontractItemEvent> items = subcontractingOrder.getItems().stream()
                .map(item -> SubcontractingOrderCreatedEvent.SubcontractItemEvent.builder()
                        .componentId(item.getComponentId())
                        .quantity(item.getQuantity())
                        .unitOfMeasure(item.getUnitOfMeasure())
                        .warehouseId(item.getWarehouseId())
                        .build())
                .collect(Collectors.toList());

        SubcontractingOrderCreatedEvent event = SubcontractingOrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .subcontractingOrderId(subcontractingOrder.getId())
                .orderNumber(subcontractingOrder.getOrderNumber())
                .vendorId(subcontractingOrder.getVendorId())
                .productionOrderId(subcontractingOrder.getProductionOrderId())
                .productId(subcontractingOrder.getProductId())
                .orderedQuantity(subcontractingOrder.getOrderedQuantity())
                .items(items)
                .build();

        rabbitTemplate.convertAndSend(exchange, "subcontracting.order.created", event);
        log.debug("Published SubcontractingOrderCreatedEvent for order {}", subcontractingOrder.getOrderNumber());
    }

    public void publishSubcontractingOrderSent(SubcontractingOrder subcontractingOrder) {
        List<SubcontractingOrderSentEvent.SentItemEvent> items = subcontractingOrder.getItems().stream()
                .map(item -> SubcontractingOrderSentEvent.SentItemEvent.builder()
                        .componentId(item.getComponentId())
                        .sentQuantity(item.getSentQuantity())
                        .unitOfMeasure(item.getUnitOfMeasure())
                        .fromWarehouseId(item.getWarehouseId())
                        .toWarehouseId(subcontractingOrder.getWarehouseId())
                        .build())
                .collect(Collectors.toList());

        SubcontractingOrderSentEvent event = SubcontractingOrderSentEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .subcontractingOrderId(subcontractingOrder.getId())
                .orderNumber(subcontractingOrder.getOrderNumber())
                .vendorId(subcontractingOrder.getVendorId())
                .sentItems(items)
                .build();

        rabbitTemplate.convertAndSend(exchange, "subcontracting.order.sent", event);
        log.debug("Published SubcontractingOrderSentEvent for order {}", subcontractingOrder.getOrderNumber());
    }

    public void publishSubcontractingReceived(SubcontractingReceipt receipt) {
        SubcontractingReceivedEvent event = SubcontractingReceivedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .subcontractingReceiptId(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .subcontractingOrderId(receipt.getSubcontractingOrder().getId())
                .productId(receipt.getProductId())
                .receivedQuantity(receipt.getReceivedQuantity())
                .acceptedQuantity(receipt.getAcceptedQuantity())
                .rejectedQuantity(receipt.getRejectedQuantity())
                .warehouseId(receipt.getWarehouseId())
                .build();

        rabbitTemplate.convertAndSend(exchange, "subcontracting.received", event);
        log.debug("Published SubcontractingReceivedEvent for receipt {}", receipt.getReceiptNumber());
    }

    public void publishQualityInspectionCreated(QualityInspection inspection) {
        QualityInspectionCreatedEvent event = QualityInspectionCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .inspectionId(inspection.getId())
                .inspectionNumber(inspection.getInspectionNumber())
                .inspectionType(inspection.getInspectionType().name())
                .productId(inspection.getProductId())
                .referenceId(getReferenceId(inspection))
                .referenceType(getReferenceType(inspection))
                .inspectorId(inspection.getInspectorId())
                .sampleSize(inspection.getSampleSize())
                .build();

        rabbitTemplate.convertAndSend(exchange, "quality.inspection.created", event);
        log.debug("Published QualityInspectionCreatedEvent for inspection {}", inspection.getInspectionNumber());
    }

    public void publishQualityInspectionPassed(QualityInspection inspection) {
        QualityInspectionPassedEvent event = QualityInspectionPassedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .inspectionId(inspection.getId())
                .inspectionNumber(inspection.getInspectionNumber())
                .productId(inspection.getProductId())
                .acceptedQuantity(inspection.getAcceptedQuantity())
                .referenceId(getReferenceId(inspection))
                .referenceType(getReferenceType(inspection))
                .warehouseId(inspection.getGoodsReceiptId() != null ? inspection.getGoodsReceiptId() :
                        inspection.getSubcontractingReceiptId() != null ? inspection.getSubcontractingReceiptId() :
                                inspection.getProductionOrderId())
                .build();

        rabbitTemplate.convertAndSend(exchange, "quality.inspection.passed", event);
        log.debug("Published QualityInspectionPassedEvent for inspection {}", inspection.getInspectionNumber());
    }

    public void publishQualityInspectionFailed(QualityInspection inspection) {
        QualityInspectionFailedEvent event = QualityInspectionFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .inspectionId(inspection.getId())
                .inspectionNumber(inspection.getInspectionNumber())
                .productId(inspection.getProductId())
                .rejectedQuantity(inspection.getRejectedQuantity())
                .disposition(inspection.getDisposition() != null ? inspection.getDisposition().name() : "REJECT")
                .referenceId(getReferenceId(inspection))
                .referenceType(getReferenceType(inspection))
                .nonConformanceIds(inspection.getNonConformances().stream().map(nc -> nc.getId()).collect(Collectors.toList()))
                .build();

        rabbitTemplate.convertAndSend(exchange, "quality.inspection.failed", event);
        log.debug("Published QualityInspectionFailedEvent for inspection {}", inspection.getInspectionNumber());
    }

    public void publishTimesheetSubmitted(Timesheet timesheet) {
        TimesheetSubmittedEvent event = TimesheetSubmittedEvent.builder()
                .eventId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .timesheetId(timesheet.getId())
                .employeeId(timesheet.getEmployeeId())
                .projectId(timesheet.getProject().getId())
                .taskId(timesheet.getTask() != null ? timesheet.getTask().getId() : null)
                .workDate(timesheet.getWorkDate())
                .hoursWorked(timesheet.getHoursWorked())
                .overtimeHours(timesheet.getOvertimeHours())
                .isBillable(timesheet.getIsBillable())
                .status(timesheet.getStatus().name())
                .build();

        rabbitTemplate.convertAndSend(exchange, "timesheet.submitted", event);
        log.debug("Published TimesheetSubmittedEvent for timesheet {}", timesheet.getId());
    }

    private UUID getReferenceId(QualityInspection inspection) {
        if (inspection.getProductionOrderId() != null) return inspection.getProductionOrderId();
        if (inspection.getWorkOrderId() != null) return inspection.getWorkOrderId();
        if (inspection.getGoodsReceiptId() != null) return inspection.getGoodsReceiptId();
        if (inspection.getSubcontractingReceiptId() != null) return inspection.getSubcontractingReceiptId();
        return null;
    }

    private String getReferenceType(QualityInspection inspection) {
        if (inspection.getProductionOrderId() != null) return "PRODUCTION_ORDER";
        if (inspection.getWorkOrderId() != null) return "WORK_ORDER";
        if (inspection.getGoodsReceiptId() != null) return "GOODS_RECEIPT";
        if (inspection.getSubcontractingReceiptId() != null) return "SUBCONTRACTING_RECEIPT";
        return "UNKNOWN";
    }
}