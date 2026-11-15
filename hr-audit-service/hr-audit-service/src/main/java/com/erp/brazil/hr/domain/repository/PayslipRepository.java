package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    Optional<Payslip> findByUuid(UUID uuid);
    Optional<Payslip> findByPayrollIdAndEmployeeId(Long payrollId, Long employeeId);
    List<Payslip> findByPayrollId(Long payrollId);
    List<Payslip> findByEmployeeId(Long employeeId);
    List<Payslip> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<Payslip> findByStatus(Payslip.PayslipStatus status);
    List<Payslip> findByAccessToken(String accessToken);
    boolean existsByPayrollIdAndEmployeeId(Long payrollId, Long employeeId);

    @Query("SELECT p FROM Payslip p WHERE p.employeeId = :employeeId AND p.payroll.referenceYear = :year ORDER BY p.payroll.referenceMonth DESC")
    List<Payslip> findByEmployeeIdAndYear(@Param("employeeId") Long employeeId, @Param("year") int year);
}