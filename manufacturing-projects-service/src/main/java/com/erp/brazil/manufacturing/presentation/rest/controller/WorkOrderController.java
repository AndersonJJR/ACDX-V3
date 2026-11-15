package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.StartOperationUseCase;
import com.erp.brazil.manufacturing.application.usecase.CompleteOperationUseCase;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderOperationRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.WorkOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Work Orders", description = "Work order management")
public class WorkOrderController {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderOperationRepository workOrderOperationRepository;
    private final StartOperationUseCase startOperationUseCase;
    private final CompleteOperationUseCase completeOperationUseCase;

    @GetMapping("/{id}")
    @Operation(summary = "Get work order by ID")
    public ResponseEntity<WorkOrderDto> getWorkOrder(@PathVariable UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));
        return ResponseEntity.ok(WorkOrderDto.fromEntity(wo));
    }

    @GetMapping
    @Operation(summary = "List work orders with pagination")
    public ResponseEntity<Page<WorkOrderDto>> listWorkOrders(
            @RequestParam(required = false) UUID productionOrderId,
            @RequestParam(required = false) UUID workCenterId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WorkOrder> orders;
        if (productionOrderId != null) {
            orders = workOrderRepository.findByProductionOrderId(productionOrderId, pageable);
        } else if (workCenterId != null && status != null) {
            orders = workOrderRepository.findByWorkCenterIdAndStatus(workCenterId, WorkOrderStatus.valueOf(status), pageable);
        } else if (workCenterId != null) {
            orders = workOrderRepository.findByWorkCenterId(workCenterId, pageable);
        } else if (status != null) {
            orders = workOrderRepository.findByStatus(WorkOrderStatus.valueOf(status), pageable);
        } else if (assignedTo != null) {
            orders = workOrderRepository.findByAssigneeAndActiveStatus(assignedTo, pageable);
        } else {
            orders = workOrderRepository.findAll(pageable);
        }
        return ResponseEntity.ok(orders.map(WorkOrderDto::fromEntity));
    }

    @GetMapping("/{id}/operations")
    @Operation(summary = "Get work order operations")
    public ResponseEntity<List<WorkOrderDto.WorkOrderOperationDto>> getWorkOrderOperations(@PathVariable UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));
        return ResponseEntity.ok(wo.getOperations().stream().map(WorkOrderDto.WorkOrderOperationDto::fromEntity).toList());
    }

    @PostMapping("/operations/{operationId}/start")
    @Operation(summary = "Start work order operation")
    public ResponseEntity<WorkOrderDto.WorkOrderOperationDto> startOperation(
            @PathVariable UUID operationId,
            @Valid @RequestBody WorkOrderDto.StartOperationRequest request) {
        var woOps = workOrderOperationRepository.findAllById(List.of(operationId));
        if (woOps.isEmpty()) {
            throw new IllegalArgumentException("Operation not found: " + operationId);
        }
        WorkOrderOperation op = woOps.get(0);

        var result = startOperationUseCase.execute(new StartOperationUseCase.StartOperationCommand(
                op.getWorkOrder().getId(),
                operationId,
                request.getEmployeeId(),
                request.getStartedBy()
        ));

        WorkOrder wo = workOrderRepository.findById(result.workOrderId()).orElseThrow();
        return ResponseEntity.ok(wo.getOperations().stream()
                .filter(o -> o.getId().equals(operationId))
                .map(WorkOrderDto.WorkOrderOperationDto::fromEntity)
                .findFirst()
                .orElseThrow());
    }

    @PostMapping("/operations/{operationId}/complete")
    @Operation(summary = "Complete work order operation")
    public ResponseEntity<WorkOrderDto.WorkOrderOperationDto> completeOperation(
            @PathVariable UUID operationId,
            @Valid @RequestBody WorkOrderDto.CompleteOperationRequest request) {
        var woOps = workOrderOperationRepository.findAllById(List.of(operationId));
        if (woOps.isEmpty()) {
            throw new IllegalArgumentException("Operation not found: " + operationId);
        }
        WorkOrderOperation op = woOps.get(0);

        var result = completeOperationUseCase.execute(new CompleteOperationUseCase.CompleteOperationCommand(
                op.getWorkOrder().getId(),
                operationId,
                request.getEmployeeId(),
                request.getQuantityCompleted(),
                request.getQuantityScrapped(),
                request.getSetupTimeMinutes(),
                request.getRunTimeMinutes(),
                request.getNotes(),
                request.getCompletedBy()
        ));

        WorkOrder wo = workOrderRepository.findById(result.workOrderId()).orElseThrow();
        return ResponseEntity.ok(wo.getOperations().stream()
                .filter(o -> o.getId().equals(operationId))
                .map(WorkOrderDto.WorkOrderOperationDto::fromEntity)
                .findFirst()
                .orElseThrow());
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Release work order")
    public ResponseEntity<WorkOrderDto> releaseWorkOrder(@PathVariable UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));

        if (wo.getStatus() != WorkOrderStatus.PLANNED) {
            throw new IllegalStateException("Can only release planned work orders");
        }

        wo.setStatus(WorkOrderStatus.RELEASED);
        workOrderRepository.save(wo);
        return ResponseEntity.ok(WorkOrderDto.fromEntity(wo));
    }

    @PatchMapping("/{id}/hold")
    @Operation(summary = "Put work order on hold")
    public ResponseEntity<WorkOrderDto> holdWorkOrder(@PathVariable UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));

        if (wo.getStatus() != WorkOrderStatus.RELEASED && wo.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only hold released or in-progress work orders");
        }

        wo.setStatus(WorkOrderStatus.ON_HOLD);
        workOrderRepository.save(wo);
        return ResponseEntity.ok(WorkOrderDto.fromEntity(wo));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete work order")
    public ResponseEntity<WorkOrderDto> completeWorkOrder(@PathVariable UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));

        if (wo.getStatus() != WorkOrderStatus.IN_PROGRESS && wo.getStatus() != WorkOrderStatus.ON_HOLD) {
            throw new IllegalStateException("Can only complete in-progress or on-hold work orders");
        }

        boolean allOpsComplete = wo.getOperations().stream()
                .allMatch(op -> op.getStatus() == com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus.COMPLETED ||
                        op.getStatus() == com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus.SKIPPED);

        if (!allOpsComplete) {
            throw new IllegalStateException("Cannot complete work order with incomplete operations");
        }

        wo.setStatus(WorkOrderStatus.COMPLETED);
        wo.setActualEndDate(LocalDateTime.now());
        workOrderRepository.save(wo);
        return ResponseEntity.ok(WorkOrderDto.fromEntity(wo));
    }

    @GetMapping("/work-center/{workCenterId}/schedule")
    @Operation(summary = "Get work center schedule")
    public ResponseEntity<Page<WorkOrderDto>> getWorkCenterSchedule(
            @PathVariable UUID workCenterId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        var orders = workOrderRepository.findByWorkCenterAndDateRange(workCenterId, startDate, endDate);
        return ResponseEntity.ok(
                org.springframework.data.domain.PageImpl(
                        orders.stream().map(WorkOrderDto::fromEntity).toList(),
                        pageable,
                        orders.size()
                )
        );
    }
}