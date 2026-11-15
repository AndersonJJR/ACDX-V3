package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.Timesheet;
import com.erp.brazil.manufacturing.domain.model.Timesheet.TimesheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    List<Timesheet> findByProjectId(UUID projectId);

    List<Timesheet> findByTaskId(UUID taskId);

    List<Timesheet> findByEmployeeId(UUID employeeId);

    List<Timesheet> findByEmployeeIdAndWorkDateBetween(UUID employeeId, LocalDate start, LocalDate end);

    List<Timesheet> findByProjectIdAndWorkDateBetween(UUID projectId, LocalDate start, LocalDate end);

    List<Timesheet> findByStatus(TimesheetStatus status);

    List<Timesheet> findByEmployeeIdAndStatus(UUID employeeId, TimesheetStatus status);

    List<Timesheet> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);

    Page<Timesheet> findByProjectId(UUID projectId, Pageable pageable);

    Page<Timesheet> findByEmployeeIdAndWorkDateBetween(UUID employeeId, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT SUM(t.hoursWorked) FROM Timesheet t WHERE t.projectId = :projectId AND t.status = 'APPROVED'")
    java.math.BigDecimal getTotalApprovedHoursByProject(@Param("projectId") UUID projectId);

    @Query("SELECT SUM(t.hoursWorked) FROM Timesheet t WHERE t.taskId = :taskId AND t.status = 'APPROVED'")
    java.math.BigDecimal getTotalApprovedHoursByTask(@Param("taskId") UUID taskId);

    @Query("SELECT SUM(t.hoursWorked) FROM Timesheet t WHERE t.employeeId = :employeeId AND t.workDate BETWEEN :start AND :end")
    java.math.BigDecimal getTotalHoursByEmployeeAndPeriod(@Param("employeeId") UUID employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}