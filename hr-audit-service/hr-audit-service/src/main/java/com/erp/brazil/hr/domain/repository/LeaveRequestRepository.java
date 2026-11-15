package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.LeaveRequest;
import com.erp.brazil.hr.domain.model.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Optional<LeaveRequest> findByUuid(String uuid);

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);

    List<LeaveRequest> findByEmployeeIdAndStatusIn(Long employeeId, List<LeaveRequestStatus> statuses);

    List<LeaveRequest> findByEmployeeIdAndStartDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.companyId = :companyId AND lr.startDate BETWEEN :startDate AND :endDate")
    List<LeaveRequest> findByCompanyIdAndPeriod(@Param("companyId") Long companyId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.companyId = :companyId AND lr.status = :status")
    List<LeaveRequest> findByCompanyIdAndStatus(@Param("companyId") Long companyId,
                                                 @Param("status") LeaveRequestStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.companyId = :companyId AND lr.status = :status AND lr.startDate BETWEEN :startDate AND :endDate")
    List<LeaveRequest> findByCompanyIdAndStatusAndPeriod(@Param("companyId") Long companyId,
                                                          @Param("status") LeaveRequestStatus status,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status AND lr.startDate <= :date")
    List<LeaveRequest> findPendingRequestsBeforeDate(@Param("status") LeaveRequestStatus status,
                                                      @Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.approvedBy = :approverId AND lr.approvedAt BETWEEN :startDate AND :endDate")
    List<LeaveRequest> findByApproverAndPeriod(@Param("approverId") Long approverId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    long countByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);

    long countByCompanyIdAndStatusAndPeriod(Long companyId, LeaveRequestStatus status, LocalDate startDate, LocalDate endDate);
}