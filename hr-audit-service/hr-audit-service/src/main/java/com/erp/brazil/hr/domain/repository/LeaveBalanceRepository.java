package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.LeaveBalance;
import com.erp.brazil.hr.domain.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndReferenceYear(Long employeeId, Long leaveTypeId, Integer year);

    List<LeaveBalance> findByEmployeeIdAndReferenceYear(Long employeeId, Integer year);

    List<LeaveBalance> findByEmployeeIdAndLeaveTypeId(Long employeeId, Long leaveTypeId);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.companyId = :companyId AND lb.referenceYear = :year")
    List<LeaveBalance> findByCompanyIdAndReferenceYear(@Param("companyId") Long companyId,
                                                        @Param("year") Integer year);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.companyId = :companyId AND lb.referenceYear = :year AND lb.leaveType.category = :category")
    List<LeaveBalance> findByCompanyIdAndYearAndCategory(@Param("companyId") Long companyId,
                                                          @Param("year") Integer year,
                                                          @Param("category") String category);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.availableDays > 0 AND lb.employee.companyId = :companyId AND lb.referenceYear = :year")
    List<LeaveBalance> findWithAvailableBalanceByCompanyAndYear(@Param("companyId") Long companyId,
                                                                 @Param("year") Integer year);

    void deleteByEmployeeIdAndReferenceYear(Long employeeId, Integer year);

    boolean existsByEmployeeIdAndLeaveTypeIdAndReferenceYear(Long employeeId, Long leaveTypeId, Integer year);
}