package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderOperationRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StartOperationUseCase {

    private final ProductionOrderOperationRepository operationRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final WorkOrderRepository workOrderRepository;

    public StartOperationUseCase(ProductionOrderOperationRepository operationRepository,
                                  ProductionOrderRepository productionOrderRepository,
                                  WorkOrderRepository workOrderRepository) {
        this.operationRepository = operationRepository;
        this.productionOrderRepository = productionOrderRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public ProductionOrderOperation startProductionOrderOperation(UUID operationId, String startedBy) {
        ProductionOrderOperation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));

        if (operation.getStatus() != ProductionOrderOperation.OperationStatus.PENDING &&
            operation.getStatus() != ProductionOrderOperation.OperationStatus.READY &&
            operation.getStatus() != ProductionOrderOperation.OperationStatus.WAITING_MATERIAL &&
            operation.getStatus() != ProductionOrderOperation.OperationStatus.WAITING_CAPACITY) {
            throw new IllegalStateException("Operation cannot be started. Current status: " + operation.getStatus());
        }

        operation.setStatus(ProductionOrderOperation.OperationStatus.IN_PROGRESS);
        operation.setActualStartDate(LocalDateTime.now());

        // Update parent production order status if needed
        ProductionOrder order = operation.getProductionOrder();
        if (order.getStatus() == ProductionOrder.ProductionOrderStatus.RELEASED) {
            order.setStatus(ProductionOrder.ProductionOrderStatus.IN_PROGRESS);
            order.setActualStartDate(LocalDateTime.now());
            productionOrderRepository.save(order);
        }

        return operationRepository.save(operation);
    }

    public WorkOrder startWorkOrder(UUID workOrderId, String startedBy) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work Order not found: " + workOrderId));

        if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.PLANNED &&
            workOrder.getStatus() != WorkOrder.WorkOrderStatus.RELEASED) {
            throw new IllegalStateException("Work Order cannot be started. Current status: " + workOrder.getStatus());
        }

        workOrder.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
        workOrder.setActualStartDate(LocalDateTime.now());

        return workOrderRepository.save(workOrder);
    }

    public void startNextOperation(UUID completedOperationId) {
        ProductionOrderOperation completed = operationRepository.findById(completedOperationId)
            .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + completedOperationId));

        List<ProductionOrderOperation> nextOps = operationRepository
            .findByProductionOrderIdOrderBySequence(completed.getProductionOrder().getId());

        boolean found = false;
        for (ProductionOrderOperation op : nextOps) {
            if (found && op.getStatus() == ProductionOrderOperation.OperationStatus.PENDING) {
                // Check if all previous operations are completed
                boolean allPrevCompleted = nextOps.stream()
                    .filter(o -> o.getSequence() < op.getSequence())
                    .allMatch(o -> o.getStatus() == ProductionOrderOperation.OperationStatus.COMPLETED ||
                                  o.getStatus() == ProductionOrderOperation.OperationStatus.SKIPPED);

                if (allPrevCompleted) {
                    op.setStatus(ProductionOrderOperation.OperationStatus.READY);
                    operationRepository.save(op);
                    break;
                }
            }
            if (op.getId().equals(completedOperationId)) {
                found = true;
            }
        }
    }
}