package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.TimeLog;
import com.erp.brazil.manufacturing.domain.model.TimeLog.LogType;
import com.erp.brazil.manufacturing.domain.model.TimeLog.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, UUID> {

    List<TimeLog> findByWorkOrderId(UUID workOrderId);

    List<TimeLog> findByWorkOrderOperationId(UUID workOrderOperationId);

    List<TimeLog> findByEmployeeId(UUID employeeId);

    List<TimeLog> findByEmployeeIdAndLogDateBetween(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate);

    Page<TimeLog> findByEmployeeId(UUID employeeId, Pageable pageable);

    @Query("SELECT tl FROM TimeLog tl WHERE tl.workOrder.id = :workOrderId AND tl.logType = :logType")
    List<TimeLog> findByWorkOrderIdAndLogType(@Param("workOrderId") UUID workOrderId, @Param("logType") LogType logType);

    @Query("SELECT SUM(tl.durationMinutes) FROM TimeLog tl WHERE tl.workOrder.id = :workOrderId")
    java.math.BigDecimal sumDurationByWorkOrder(@Param("workOrderId") UUID workOrderId);

    @Query("SELECT SUM(tl.durationMinutes) FROM TimeLog tl WHERE tl.workOrderOperationId = :workOrderOperationId")
    java.math.BigDecimal sumDurationByWorkOrderOperation(@Param("workOrderOperationId") UUID workOrderOperationId);

    List<TimeLog> findByApprovalStatus(ApprovalStatus approvalStatus);

    @Query("SELECT tl FROM TimeLog tl WHERE tl.approvalStatus = :status AND tl.logDate BETWEEN :startDate AND :endDate")
    List<TimeLog> findByApprovalStatusAndDateRange(@Param("status") ApprovalStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}