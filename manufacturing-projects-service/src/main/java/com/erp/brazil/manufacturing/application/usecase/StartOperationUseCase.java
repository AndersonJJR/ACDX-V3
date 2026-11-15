package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.application.event.ProductionOrderEventPublisher;
import com.erp.brazil.manufacturing.domain.model.TimeLog;
import com.erp.brazil.manufacturing.domain.model.TimeLog.LogType;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus;
import com.erp.brazil.manufacturing.domain.repository.TimeLogRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartOperationUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderOperationRepository workOrderOperationRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProductionOrderEventPublisher eventPublisher;

    public record StartOperationCommand(
            UUID workOrderId,
            UUID operationId,
            UUID employeeId,
            String startedBy
    ) {}

    public record StartOperationResult(
            UUID workOrderId,
            UUID operationId,
            OperationStatus operationStatus,
            WorkOrderStatus workOrderStatus,
            LocalDateTime actualStartDate
    ) {}

    @Transactional
    public StartOperationResult execute(StartOperationCommand command) {
        log.info("Starting operation {} for work order {}", command.operationId(), command.workOrderId());

        WorkOrder workOrder = workOrderRepository.findById(command.workOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + command.workOrderId()));

        WorkOrderOperation operation = workOrderOperationRepository.findById(command.operationId())
                .orElseThrow(() -> new IllegalArgumentException("Work order operation not found: " + command.operationId()));

        if (!operation.getWorkOrder().getId().equals(command.workOrderId())) {
            throw new IllegalArgumentException("Operation does not belong to work order");
        }

        if (operation.getStatus() == OperationStatus.COMPLETED || operation.getStatus() == OperationStatus.CANCELLED) {
            throw new IllegalStateException("Operation is already " + operation.getStatus());
        }

        operation.setStatus(OperationStatus.IN_PROGRESS);
        operation.setActualStartDate(LocalDateTime.now());

        if (workOrder.getStatus() == WorkOrderStatus.PLANNED || workOrder.getStatus() == WorkOrderStatus.RELEASED) {
            workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
            workOrder.setActualStartDate(LocalDateTime.now());
        }

        TimeLog timeLog = TimeLog.builder()
                .workOrder(workOrder)
                .workOrderOperationId(command.operationId())
                .employeeId(command.employeeId())
                .logDate(LocalDateTime.now())
                .logType(LogType.SETUP)
                .startTime(LocalDateTime.now())
                .approvalStatus(TimeLog.ApprovalStatus.AUTO_APPROVED)
                .description("Operation started")
                .createdBy(command.startedBy())
                .build();
        timeLogRepository.save(timeLog);

        workOrderOperationRepository.save(operation);
        workOrderRepository.save(workOrder);

        eventPublisher.publishOperationStarted(workOrder, operation, command.employeeId());

        return new StartOperationResult(
                workOrder.getId(),
                operation.getId(),
                operation.getStatus(),
                workOrder.getStatus(),
                operation.getActualStartDate()
        );
    }
}