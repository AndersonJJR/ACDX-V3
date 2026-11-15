package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findByUuid(UUID uuid);
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndStartDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<LeaveRequest> findByLeaveTypeId(Long leaveTypeId);
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);
    List<LeaveRequest> findByApprovedBy(Long approvedBy);
    List<LeaveRequest> findOverlappingRequests(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employeeId = :employeeId " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate " +
           "AND l.status IN (:statuses) " +
           "AND (:excludeId IS NULL OR l.id != :excludeId)")
    List<LeaveRequest> findOverlapping(@Param("employeeId") Long employeeId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("statuses") List<LeaveRequest.LeaveStatus> statuses,
                                        @Param("excludeId") Long excludeId);

    long countByEmployeeIdAndStatusAndStartDateBetween(Long employeeId, LeaveRequest.LeaveStatus status, LocalDate start, LocalDate end);
}