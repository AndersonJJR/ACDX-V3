package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.application.event.ProductionOrderEventPublisher;
import com.erp.brazil.manufacturing.domain.model.TimeLog;
import com.erp.brazil.manufacturing.domain.model.TimeLog.LogType;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import com.erp.brazil.manufacturing.domain.repository.TimeLogRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteOperationUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderOperationRepository workOrderOperationRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProductionOrderEventPublisher eventPublisher;

    public record CompleteOperationCommand(
            UUID workOrderId,
            UUID operationId,
            UUID employeeId,
            BigDecimal quantityCompleted,
            BigDecimal quantityScrapped,
            BigDecimal setupTimeMinutes,
            BigDecimal runTimeMinutes,
            String notes,
            String completedBy
    ) {}

    public record CompleteOperationResult(
            UUID workOrderId,
            UUID operationId,
            OperationStatus operationStatus,
            WorkOrderStatus workOrderStatus,
            BigDecimal completedQuantity,
            BigDecimal scrappedQuantity
    ) {}

    @Transactional
    public CompleteOperationResult execute(CompleteOperationCommand command) {
        log.info("Completing operation {} for work order {}", command.operationId(), command.workOrderId());

        WorkOrder workOrder = workOrderRepository.findById(command.workOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + command.workOrderId()));

        WorkOrderOperation operation = workOrderOperationRepository.findById(command.operationId())
                .orElseThrow(() -> new IllegalArgumentException("Work order operation not found: " + command.operationId()));

        if (!operation.getWorkOrder().getId().equals(command.workOrderId())) {
            throw new IllegalArgumentException("Operation does not belong to work order");
        }

        if (operation.getStatus() == OperationStatus.COMPLETED) {
            throw new IllegalStateException("Operation is already completed");
        }

        LocalDateTime endTime = LocalDateTime.now();

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setActualEndDate(endTime);
        operation.setCompletedQuantity(command.quantityCompleted() != null ? command.quantityCompleted() : BigDecimal.ZERO);
        operation.setScrappedQuantity(command.quantityScrapped() != null ? command.quantityScrapped() : BigDecimal.ZERO);
        operation.setActualSetupTimeMinutes(command.setupTimeMinutes() != null ? command.setupTimeMinutes() : BigDecimal.ZERO);
        operation.setActualRunTimeMinutes(command.runTimeMinutes() != null ? command.runTimeMinutes() : BigDecimal.ZERO);
        operation.setNotes(command.notes());

        workOrder.setCompletedQuantity(workOrder.getCompletedQuantity().add(command.quantityCompleted() != null ? command.quantityCompleted() : BigDecimal.ZERO));
        workOrder.setScrappedQuantity(workOrder.getScrappedQuantity().add(command.quantityScrapped() != null ? command.quantityScrapped() : BigDecimal.ZERO));
        workOrder.setActualSetupTimeMinutes(workOrder.getActualSetupTimeMinutes().add(command.setupTimeMinutes() != null ? command.setupTimeMinutes() : BigDecimal.ZERO));
        workOrder.setActualRunTimeMinutes(workOrder.getActualRunTimeMinutes().add(command.runTimeMinutes() != null ? command.runTimeMinutes() : BigDecimal.ZERO));

        List<WorkOrderOperation> allOps = workOrderOperationRepository.findByWorkOrderIdOrderBySequence(command.workOrderId());
        boolean allCompleted = allOps.stream().allMatch(op -> op.getStatus() == OperationStatus.COMPLETED || op.getStatus() == OperationStatus.SKIPPED || op.getStatus() == OperationStatus.CANCELLED);

        if (allCompleted) {
            workOrder.setStatus(WorkOrderStatus.COMPLETED);
            workOrder.setActualEndDate(endTime);
        }

        TimeLog timeLog = TimeLog.builder()
                .workOrder(workOrder)
                .workOrderOperationId(command.operationId())
                .employeeId(command.employeeId())
                .logDate(endTime)
                .logType(LogType.RUN)
                .startTime(operation.getActualStartDate() != null ? operation.getActualStartDate() : endTime)
                .endTime(endTime)
                .durationMinutes(command.setupTimeMinutes().add(command.runTimeMinutes()))
                .setupTimeMinutes(command.setupTimeMinutes())
                .runTimeMinutes(command.runTimeMinutes())
                .quantityCompleted(command.quantityCompleted())
                .quantityScrapped(command.quantityScrapped())
                .description("Operation completed" + (command.notes() != null ? ": " + command.notes() : ""))
                .approvalStatus(TimeLog.ApprovalStatus.AUTO_APPROVED)
                .createdBy(command.completedBy())
                .build();
        timeLogRepository.save(timeLog);

        workOrderOperationRepository.save(operation);
        workOrderRepository.save(workOrder);

        eventPublisher.publishOperationCompleted(workOrder, operation, command.employeeId());

        return new CompleteOperationResult(
                workOrder.getId(),
                operation.getId(),
                operation.getStatus(),
                workOrder.getStatus(),
                workOrder.getCompletedQuantity(),
                workOrder.getScrappedQuantity()
        );
    }
}