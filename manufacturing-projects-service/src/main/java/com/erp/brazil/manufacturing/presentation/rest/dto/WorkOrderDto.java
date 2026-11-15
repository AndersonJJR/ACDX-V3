package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDto {

    private UUID id;
    private String orderNumber;
    private UUID productionOrderId;
    private UUID productionOrderOperationId;
    private UUID workCenterId;
    private UUID workStationId;
    private String status;
    private String priority;
    private String operationName;
    private String description;
    private BigDecimal plannedQuantity;
    private BigDecimal startedQuantity;
    private BigDecimal completedQuantity;
    private BigDecimal scrappedQuantity;
    private String unitOfMeasure;
    private BigDecimal plannedSetupTimeMinutes;
    private BigDecimal plannedRunTimeMinutes;
    private BigDecimal actualSetupTimeMinutes;
    private BigDecimal actualRunTimeMinutes;
    private LocalDateTime plannedStartDate;
    private LocalDateTime plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private String assignedTo;
    private String notes;
    private List<WorkOrderOperationDto> operations;
    private List<TimeLogDto> timeLogs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static WorkOrderDto fromEntity(WorkOrder wo) {
        return WorkOrderDto.builder()
                .id(wo.getId())
                .orderNumber(wo.getOrderNumber())
                .productionOrderId(wo.getProductionOrder().getId())
                .productionOrderOperationId(wo.getProductionOrderOperationId())
                .workCenterId(wo.getWorkCenterId())
                .workStationId(wo.getWorkStationId())
                .status(wo.getStatus().name())
                .priority(wo.getPriority().name())
                .operationName(wo.getOperationName())
                .description(wo.getDescription())
                .plannedQuantity(wo.getPlannedQuantity())
                .startedQuantity(wo.getStartedQuantity())
                .completedQuantity(wo.getCompletedQuantity())
                .scrappedQuantity(wo.getScrappedQuantity())
                .unitOfMeasure(wo.getUnitOfMeasure())
                .plannedSetupTimeMinutes(wo.getPlannedSetupTimeMinutes())
                .plannedRunTimeMinutes(wo.getPlannedRunTimeMinutes())
                .actualSetupTimeMinutes(wo.getActualSetupTimeMinutes())
                .actualRunTimeMinutes(wo.getActualRunTimeMinutes())
                .plannedStartDate(wo.getPlannedStartDate())
                .plannedEndDate(wo.getPlannedEndDate())
                .actualStartDate(wo.getActualStartDate())
                .actualEndDate(wo.getActualEndDate())
                .assignedTo(wo.getAssignedTo())
                .notes(wo.getNotes())
                .operations(wo.getOperations().stream().map(WorkOrderOperationDto::fromEntity).toList())
                .timeLogs(wo.getTimeLogs().stream().map(TimeLogDto::fromEntity).toList())
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .createdBy(wo.getCreatedBy())
                .updatedBy(wo.getUpdatedBy())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkOrderOperationDto {
        private UUID id;
        private Integer sequence;
        private String operationName;
        private String description;
        private UUID workCenterId;
        private UUID workStationId;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private String status;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private String assignedTo;
        private String notes;

        public static WorkOrderOperationDto fromEntity(WorkOrderOperation op) {
            return WorkOrderOperationDto.builder()
                    .id(op.getId())
                    .sequence(op.getSequence())
                    .operationName(op.getOperationName())
                    .description(op.getDescription())
                    .workCenterId(op.getWorkCenterId())
                    .workStationId(op.getWorkStationId())
                    .plannedSetupTimeMinutes(op.getPlannedSetupTimeMinutes())
                    .plannedRunTimeMinutes(op.getPlannedRunTimeMinutes())
                    .actualSetupTimeMinutes(op.getActualSetupTimeMinutes())
                    .actualRunTimeMinutes(op.getActualRunTimeMinutes())
                    .status(op.getStatus().name())
                    .plannedStartDate(op.getPlannedStartDate())
                    .plannedEndDate(op.getPlannedEndDate())
                    .actualStartDate(op.getActualStartDate())
                    .actualEndDate(op.getActualEndDate())
                    .assignedTo(op.getAssignedTo())
                    .notes(op.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeLogDto {
        private UUID id;
        private UUID workOrderId;
        private UUID workOrderOperationId;
        private UUID employeeId;
        private LocalDateTime logDate;
        private String logType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal durationMinutes;
        private BigDecimal setupTimeMinutes;
        private BigDecimal runTimeMinutes;
        private BigDecimal quantityCompleted;
        private BigDecimal quantityScrapped;
        private String description;
        private Boolean isOvertime;
        private BigDecimal overtimeMultiplier;
        private String approvalStatus;
        private String approvedBy;
        private LocalDateTime approvedAt;

        public static TimeLogDto fromEntity(TimeLog tl) {
            return TimeLogDto.builder()
                    .id(tl.getId())
                    .workOrderId(tl.getWorkOrder().getId())
                    .workOrderOperationId(tl.getWorkOrderOperationId())
                    .employeeId(tl.getEmployeeId())
                    .logDate(tl.getLogDate())
                    .logType(tl.getLogType().name())
                    .startTime(tl.getStartTime())
                    .endTime(tl.getEndTime())
                    .durationMinutes(tl.getDurationMinutes())
                    .setupTimeMinutes(tl.getSetupTimeMinutes())
                    .runTimeMinutes(tl.getRunTimeMinutes())
                    .quantityCompleted(tl.getQuantityCompleted())
                    .quantityScrapped(tl.getQuantityScrapped())
                    .description(tl.getDescription())
                    .isOvertime(tl.getIsOvertime())
                    .overtimeMultiplier(tl.getOvertimeMultiplier())
                    .approvalStatus(tl.getApprovalStatus().name())
                    .approvedBy(tl.getApprovedBy())
                    .approvedAt(tl.getApprovedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartOperationRequest {
        private UUID employeeId;
        private String startedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteOperationRequest {
        private UUID employeeId;
        private BigDecimal quantityCompleted;
        private BigDecimal quantityScrapped;
        private BigDecimal setupTimeMinutes;
        private BigDecimal runTimeMinutes;
        private String notes;
        private String completedBy;
    }
}