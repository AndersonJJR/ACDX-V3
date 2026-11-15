package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderOperationRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CompleteOperationUseCase {

    private final ProductionOrderOperationRepository operationRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final WorkOrderRepository workOrderRepository;

    public CompleteOperationUseCase(ProductionOrderOperationRepository operationRepository,
                                     ProductionOrderRepository productionOrderRepository,
                                     WorkOrderRepository workOrderRepository) {
        this.operationRepository = operationRepository;
        this.productionOrderRepository = productionOrderRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public ProductionOrderOperation completeProductionOrderOperation(UUID operationId,
                                                                      BigDecimal completedQuantity,
                                                                      BigDecimal scrappedQuantity,
                                                                      BigDecimal actualSetupTime,
                                                                      BigDecimal actualRunTime,
                                                                      String completedBy) {
        ProductionOrderOperation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));

        if (operation.getStatus() != ProductionOrderOperation.OperationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Operation is not in progress. Current status: " + operation.getStatus());
        }

        operation.setStatus(ProductionOrderOperation.OperationStatus.COMPLETED);
        operation.setActualEndDate(LocalDateTime.now());
        operation.setCompletedQuantity(completedQuantity);
        operation.setScrappedQuantity(scrappedQuantity);
        operation.setActualSetupTimeMinutes(actualSetupTime);
        operation.setActualRunTimeMinutes(actualRunTime);

        // Update parent production order
        ProductionOrder order = operation.getProductionOrder();
        order.setProducedQuantity(order.getProducedQuantity().add(completedQuantity));
        order.setScrappedQuantity(order.getScrappedQuantity().add(scrappedQuantity));
        order.setTotalSetupTimeMinutes(order.getTotalSetupTimeMinutes().add(actualSetupTime));
        order.setTotalRunTimeMinutes(order.getTotalRunTimeMinutes().add(actualRunTime));

        // Check if all operations are completed
        List<ProductionOrderOperation> allOps = operationRepository.findByProductionOrderIdOrderBySequence(order.getId());
        boolean allCompleted = allOps.stream()
            .allMatch(op -> op.getStatus() == ProductionOrderOperation.OperationStatus.COMPLETED ||
                           op.getStatus() == ProductionOrderOperation.OperationStatus.SKIPPED);

        if (allCompleted) {
            order.setStatus(ProductionOrder.ProductionOrderStatus.COMPLETED);
            order.setActualEndDate(LocalDateTime.now());
            order.setCompletedDate(LocalDateTime.now());
        }

        productionOrderRepository.save(order);
        return operationRepository.save(operation);
    }

    public WorkOrder completeWorkOrder(UUID workOrderId,
                                        BigDecimal completedQuantity,
                                        BigDecimal scrappedQuantity,
                                        BigDecimal actualSetupTime,
                                        BigDecimal actualRunTime,
                                        String completedBy) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work Order not found: " + workOrderId));

        if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Work Order is not in progress. Current status: " + workOrder.getStatus());
        }

        workOrder.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);
        workOrder.setActualEndDate(LocalDateTime.now());
        workOrder.setCompletedQuantity(completedQuantity);
        workOrder.setScrappedQuantity(scrappedQuantity);
        workOrder.setActualSetupTimeMinutes(actualSetupTime);
        workOrder.setActualRunTimeMinutes(actualRunTime);

        // Update parent production order operation if linked
        if (workOrder.getProductionOrderOperationId() != null) {
            ProductionOrderOperation poOp = operationRepository.findById(workOrder.getProductionOrderOperationId())
                .orElse(null);
            if (poOp != null) {
                poOp.setCompletedQuantity(poOp.getCompletedQuantity().add(completedQuantity));
                poOp.setScrappedQuantity(poOp.getScrappedQuantity().add(scrappedQuantity));
                operationRepository.save(poOp);
            }
        }

        return workOrderRepository.save(workOrder);
    }

    public void skipOperation(UUID operationId, String skippedBy) {
        ProductionOrderOperation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));

        operation.setStatus(ProductionOrderOperation.OperationStatus.SKIPPED);
        operation.setActualEndDate(LocalDateTime.now());
        operationRepository.save(operation);

        // Trigger next operation
        startNextOperation(operationId);
    }

    private void startNextOperation(UUID completedOperationId) {
        ProductionOrderOperation completed = operationRepository.findById(completedOperationId)
            .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + completedOperationId));

        List<ProductionOrderOperation> nextOps = operationRepository
            .findByProductionOrderIdOrderBySequence(completed.getProductionOrder().getId());

        boolean found = false;
        for (ProductionOrderOperation op : nextOps) {
            if (found && op.getStatus() == ProductionOrderOperation.OperationStatus.PENDING) {
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