package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByUuid(UUID uuid);
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndReferenceYear(Long employeeId, Long leaveTypeId, int year);
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    List<LeaveBalance> findByEmployeeIdAndReferenceYear(Long employeeId, int year);
    List<LeaveBalance> findByLeaveTypeIdAndReferenceYear(Long leaveTypeId, int year);
    List<LeaveBalance> findByReferenceYear(int year);
    List<LeaveBalance> findWithAvailableDaysGreaterThan(BigDecimal minDays);

    @Query("SELECT l FROM LeaveBalance l WHERE l.employeeId = :employeeId AND l.referenceYear = :year AND l.availableDays > 0")
    List<LeaveBalance> findAvailableBalances(@Param("employeeId") Long employeeId, @Param("year") int year);

    boolean existsByEmployeeIdAndLeaveTypeIdAndReferenceYear(Long employeeId, Long leaveTypeId, int year);
}