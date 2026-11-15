package com.erp.brazil.manufacturing.infrastructure.scheduling;

import com.erp.brazil.manufacturing.domain.model.WorkCenter;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus;
import com.erp.brazil.manufacturing.domain.repository.WorkCenterRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingEngine {

    private final WorkCenterRepository workCenterRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderOperationRepository workOrderOperationRepository;

    private final Map<UUID, WorkCenterSchedule> scheduleCache = new ConcurrentHashMap<>();

    public record ScheduleResult(
            UUID workOrderId,
            UUID workCenterId,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd,
            boolean finiteCapacity,
            List<String> conflicts
    ) {}

    public record WorkCenterSchedule(
            UUID workCenterId,
            BigDecimal capacityPerDayMinutes,
            BigDecimal efficiencyPercentage,
            BigDecimal utilizationPercentage,
            LocalTime shiftStart,
            LocalTime shiftEnd,
            Set<DayOfWeek> workingDays,
            Map<LocalDateTime, BigDecimal> bookedMinutes
    ) {}

    @Transactional(readOnly = true)
    public List<ScheduleResult> scheduleWorkOrdersFiniteCapacity(List<UUID> workOrderIds, LocalDateTime horizonStart, LocalDateTime horizonEnd) {
        log.info("Scheduling {} work orders with finite capacity from {} to {}", workOrderIds.size(), horizonStart, horizonEnd);

        List<WorkOrder> workOrders = workOrderRepository.findAllById(workOrderIds);
        workOrders.sort(Comparator.comparing(WorkOrder::getPriority, Comparator.reverseOrder())
                .thenComparing(WorkOrder::getPlannedStartDate, Comparator.nullsLast(Comparator.naturalOrder())));

        List<ScheduleResult> results = new ArrayList<>();

        for (WorkOrder workOrder : workOrders) {
            if (workOrder.getStatus() == WorkOrderStatus.COMPLETED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
                continue;
            }

            ScheduleResult result = scheduleWorkOrderFinite(workOrder, horizonStart, horizonEnd);
            results.add(result);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public ScheduleResult scheduleWorkOrderFinite(WorkOrder workOrder, LocalDateTime horizonStart, LocalDateTime horizonEnd) {
        UUID workCenterId = workOrder.getWorkCenterId();
        if (workCenterId == null) {
            return new ScheduleResult(workOrder.getId(), null, workOrder.getPlannedStartDate(), workOrder.getPlannedEndDate(),
                    false, List.of("No work center assigned"));
        }

        WorkCenter workCenter = workCenterRepository.findById(workCenterId)
                .orElseThrow(() -> new IllegalArgumentException("Work center not found: " + workCenterId));

        WorkCenterSchedule schedule = getOrBuildSchedule(workCenter, horizonStart, horizonEnd);

        BigDecimal totalMinutes = workOrder.getPlannedSetupTimeMinutes().add(workOrder.getPlannedRunTimeMinutes());
        BigDecimal availableMinutesPerDay = schedule.capacityPerDayMinutes()
                .multiply(schedule.efficiencyPercentage().divide(BigDecimal.valueOf(100)))
                .multiply(schedule.utilizationPercentage().divide(BigDecimal.valueOf(100)));

        LocalDateTime start = findAvailableSlot(schedule, horizonStart, totalMinutes, availableMinutesPerDay);
        LocalDateTime end = calculateEndTime(schedule, start, totalMinutes, availableMinutesPerDay);

        if (end.isAfter(horizonEnd)) {
            return new ScheduleResult(workOrder.getId(), workCenterId, null, null, true,
                    List.of("Cannot schedule within horizon"));
        }

        bookSlot(schedule, start, end);

        return new ScheduleResult(workOrder.getId(), workCenterId, start, end, true, List.of());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResult> scheduleOperationsInfiniteCapacity(UUID workCenterId, LocalDateTime start, LocalDateTime end) {
        log.info("Scheduling operations with infinite capacity for work center {}", workCenterId);

        List<WorkOrderOperation> operations = workOrderOperationRepository.findByWorkCenterIdAndStatus(
                workCenterId, OperationStatus.PENDING);

        operations.sort(Comparator.comparing(WorkOrderOperation::getPlannedStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WorkOrderOperation::getSequence));

        List<ScheduleResult> results = new ArrayList<>();
        LocalDateTime current = start;

        for (WorkOrderOperation op : operations) {
            BigDecimal totalMinutes = op.getPlannedSetupTimeMinutes().add(op.getPlannedRunTimeMinutes());
            LocalDateTime opEnd = current.plusMinutes(totalMinutes.longValue());

            results.add(new ScheduleResult(op.getWorkOrder().getId(), workCenterId, current, opEnd, false, List.of()));
            current = opEnd;
        }

        return results;
    }

    @Transactional(readOnly = true)
    public ScheduleResult rescheduleWorkOrder(UUID workOrderId, LocalDateTime newStart) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        UUID workCenterId = workOrder.getWorkCenterId();
        if (workCenterId == null) {
            throw new IllegalStateException("Work order has no work center assigned");
        }

        WorkCenter workCenter = workCenterRepository.findById(workCenterId)
                .orElseThrow(() -> new IllegalArgumentException("Work center not found: " + workCenterId));

        BigDecimal totalMinutes = workOrder.getPlannedSetupTimeMinutes().add(workOrder.getPlannedRunTimeMinutes());
        BigDecimal availableMinutesPerDay = workCenter.getCapacityPerDayMinutes()
                .multiply(workCenter.getEfficiencyPercentage().divide(BigDecimal.valueOf(100)))
                .multiply(workCenter.getUtilizationPercentage().divide(BigDecimal.valueOf(100)));

        WorkCenterSchedule schedule = getOrBuildSchedule(workCenter, newStart, newStart.plusDays(30));
        LocalDateTime end = calculateEndTime(schedule, newStart, totalMinutes, availableMinutesPerDay);

        if (hasConflict(schedule, newStart, end)) {
            return new ScheduleResult(workOrderId, workCenterId, null, null, true,
                    List.of("Time slot conflict at new start time"));
        }

        bookSlot(schedule, newStart, end);

        workOrder.setPlannedStartDate(newStart);
        workOrder.setPlannedEndDate(end);
        workOrderRepository.save(workOrder);

        return new ScheduleResult(workOrderId, workCenterId, newStart, end, true, List.of());
    }

    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getWorkCenterUtilization(LocalDateTime start, LocalDateTime end) {
        List<WorkCenter> workCenters = workCenterRepository.findByStatus(WorkCenter.WorkCenterStatus.ACTIVE);
        Map<UUID, BigDecimal> utilization = new HashMap<>();

        for (WorkCenter wc : workCenters) {
            BigDecimal totalCapacity = wc.getCapacityPerDayMinutes()
                    .multiply(BigDecimal.valueOf(calculateWorkingDays(start, end, wc)))
                    .multiply(wc.getEfficiencyPercentage().divide(BigDecimal.valueOf(100)));

            List<WorkOrder> orders = workOrderRepository.findByWorkCenterIdAndDateRange(wc.getId(), start, end);
            BigDecimal bookedMinutes = orders.stream()
                    .filter(o -> o.getStatus() != WorkOrderStatus.CANCELLED && o.getStatus() != WorkOrderStatus.COMPLETED)
                    .map(o -> o.getPlannedSetupTimeMinutes().add(o.getPlannedRunTimeMinutes()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal utilPct = totalCapacity.compareTo(BigDecimal.ZERO) > 0
                    ? bookedMinutes.divide(totalCapacity, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            utilization.put(wc.getId(), utilPct.min(BigDecimal.valueOf(100)));
        }

        return utilization;
    }

    private WorkCenterSchedule getOrBuildSchedule(WorkCenter workCenter, LocalDateTime start, LocalDateTime end) {
        return scheduleCache.computeIfAbsent(workCenter.getId(), k -> buildSchedule(workCenter, start, end));
    }

    private WorkCenterSchedule buildSchedule(WorkCenter workCenter, LocalDateTime start, LocalDateTime end) {
        Set<DayOfWeek> workingDays = EnumSet.allOf(DayOfWeek.class);
        workingDays.remove(DayOfWeek.SATURDAY);
        workingDays.remove(DayOfWeek.SUNDAY);

        Map<LocalDateTime, BigDecimal> bookedMinutes = new HashMap<>();

        List<WorkOrder> existingOrders = workOrderRepository.findByWorkCenterIdAndDateRange(workCenter.getId(), start, end);
        for (WorkOrder order : existingOrders) {
            if (order.getStatus() != WorkOrderStatus.CANCELLED && order.getStatus() != WorkOrderStatus.COMPLETED) {
                LocalDateTime current = order.getPlannedStartDate();
                LocalDateTime orderEnd = order.getPlannedEndDate();
                BigDecimal totalMinutes = order.getPlannedSetupTimeMinutes().add(order.getPlannedRunTimeMinutes());
                BigDecimal dailyCapacity = workCenter.getCapacityPerDayMinutes()
                        .multiply(workCenter.getEfficiencyPercentage().divide(BigDecimal.valueOf(100)))
                        .multiply(workCenter.getUtilizationPercentage().divide(BigDecimal.valueOf(100)));

                while (current.isBefore(orderEnd) && !current.isAfter(end)) {
                    LocalDateTime dayStart = current.withHour(8).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime dayEnd = dayStart.plusMinutes(dailyCapacity.longValue());
                    BigDecimal minutesForDay = totalMinutes.min(dailyCapacity);

                    bookedMinutes.merge(dayStart, minutesForDay, BigDecimal::add);
                    current = current.plusDays(1);
                }
            }
        }

        return new WorkCenterSchedule(
                workCenter.getId(),
                workCenter.getCapacityPerDayMinutes(),
                workCenter.getEfficiencyPercentage(),
                workCenter.getUtilizationPercentage(),
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                workingDays,
                bookedMinutes
        );
    }

    private LocalDateTime findAvailableSlot(WorkCenterSchedule schedule, LocalDateTime start, BigDecimal requiredMinutes, BigDecimal dailyCapacity) {
        LocalDateTime current = start;

        while (true) {
            if (!schedule.workingDays().contains(current.getDayOfWeek())) {
                current = current.plusDays(1).with(schedule.shiftStart());
                continue;
            }

            LocalDateTime dayStart = current.with(schedule.shiftStart());
            LocalDateTime dayEnd = current.with(schedule.shiftEnd());

            BigDecimal booked = schedule.bookedMinutes().getOrDefault(dayStart, BigDecimal.ZERO);
            BigDecimal available = dailyCapacity.subtract(booked);

            if (available.compareTo(requiredMinutes) >= 0) {
                return dayStart.plusMinutes(booked.longValue());
            }

            current = current.plusDays(1).with(schedule.shiftStart());
        }
    }

    private LocalDateTime calculateEndTime(WorkCenterSchedule schedule, LocalDateTime start, BigDecimal requiredMinutes, BigDecimal dailyCapacity) {
        LocalDateTime current = start;
        BigDecimal remaining = requiredMinutes;

        while (remaining.compareTo(BigDecimal.ZERO) > 0) {
            if (!schedule.workingDays().contains(current.getDayOfWeek())) {
                current = current.plusDays(1).with(schedule.shiftStart());
                continue;
            }

            LocalDateTime dayEnd = current.with(schedule.shiftEnd());
            BigDecimal booked = schedule.bookedMinutes().getOrDefault(current.with(schedule.shiftStart()), BigDecimal.ZERO);
            BigDecimal availableToday = dailyCapacity.subtract(booked);

            BigDecimal toUse = remaining.min(availableToday);
            current = current.plusMinutes(toUse.longValue());
            remaining = remaining.subtract(toUse);

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                current = current.plusDays(1).with(schedule.shiftStart());
            }
        }

        return current;
    }

    private void bookSlot(WorkCenterSchedule schedule, LocalDateTime start, LocalDateTime end) {
        LocalDateTime current = start;
        BigDecimal totalMinutes = BigDecimal.valueOf(java.time.Duration.between(start, end).toMinutes());

        while (current.isBefore(end)) {
            if (!schedule.workingDays().contains(current.getDayOfWeek())) {
                current = current.plusDays(1).with(schedule.shiftStart());
                continue;
            }

            LocalDateTime dayStart = current.with(schedule.shiftStart());
            LocalDateTime dayEnd = current.with(schedule.shiftEnd());
            BigDecimal dailyCapacity = schedule.capacityPerDayMinutes()
                    .multiply(schedule.efficiencyPercentage().divide(BigDecimal.valueOf(100)))
                    .multiply(schedule.utilizationPercentage().divide(BigDecimal.valueOf(100)));

            BigDecimal booked = schedule.bookedMinutes().getOrDefault(dayStart, BigDecimal.ZERO);
            BigDecimal available = dailyCapacity.subtract(booked);
            BigDecimal toBook = totalMinutes.min(available);

            schedule.bookedMinutes().merge(dayStart, toBook, BigDecimal::add);
            totalMinutes = totalMinutes.subtract(toBook);
            current = current.plusDays(1).with(schedule.shiftStart());
        }
    }

    private boolean hasConflict(WorkCenterSchedule schedule, LocalDateTime start, LocalDateTime end) {
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            if (schedule.workingDays().contains(current.getDayOfWeek())) {
                LocalDateTime dayStart = current.with(schedule.shiftStart());
                BigDecimal booked = schedule.bookedMinutes().getOrDefault(dayStart, BigDecimal.ZERO);
                BigDecimal dailyCapacity = schedule.capacityPerDayMinutes()
                        .multiply(schedule.efficiencyPercentage().divide(BigDecimal.valueOf(100)))
                        .multiply(schedule.utilizationPercentage().divide(BigDecimal.valueOf(100)));

                BigDecimal needed = BigDecimal.valueOf(java.time.Duration.between(
                        current.with(schedule.shiftStart()).isAfter(start) ? current.with(schedule.shiftStart()) : start,
                        current.with(schedule.shiftEnd()).isBefore(end) ? current.with(schedule.shiftEnd()) : end
                ).toMinutes());

                if (booked.add(needed).compareTo(dailyCapacity) > 0) {
                    return true;
                }
            }
            current = current.plusDays(1);
        }
        return false;
    }

    private int calculateWorkingDays(LocalDateTime start, LocalDateTime end, WorkCenter wc) {
        int days = 0;
        LocalDateTime current = start;
        while (!current.isAfter(end)) {
            if (wc.getCalendarId() != null) {
                // TODO: Check calendar for holidays
            }
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    public void clearCache(UUID workCenterId) {
        scheduleCache.remove(workCenterId);
    }

    public void clearAllCache() {
        scheduleCache.clear();
    }
}