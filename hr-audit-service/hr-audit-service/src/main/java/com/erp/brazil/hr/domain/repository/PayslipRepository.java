package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Payslip;
import com.erp.brazil.hr.domain.model.PayslipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    Optional<Payslip> findByPayrollIdAndEmployeeId(Long payrollId, Long employeeId);

    Optional<Payslip> findByUuid(String uuid);

    Optional<Payslip> findByAccessToken(String accessToken);

    List<Payslip> findByPayrollId(Long payrollId);

    Page<Payslip> findByPayrollId(Long payrollId, Pageable pageable);

    List<Payslip> findByEmployeeId(Long employeeId);

    Page<Payslip> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Payslip> findByEmployeeIdAndStatus(Long employeeId, PayslipStatus status);

    @Query("SELECT p FROM Payslip p WHERE p.payroll.companyId = :companyId AND p.payroll.referenceYear = :year AND p.payroll.referenceMonth = :month")
    List<Payslip> findByCompanyIdAndPeriod(@Param("companyId") Long companyId,
                                            @Param("year") Integer year,
                                            @Param("month") Integer month);

    @Query("SELECT p FROM Payslip p WHERE p.accessToken = :token AND p.accessExpiresAt > :now")
    Optional<Payslip> findValidByAccessToken(@Param("token") String token,
                                              @Param("now") java.time.Instant now);

    @Query("SELECT p FROM Payslip p WHERE p.status = :status AND p.accessExpiresAt < :now")
    List<Payslip> findExpiredAccessTokens(@Param("status") PayslipStatus status,
                                           @Param("now") java.time.Instant now);

    long countByPayrollIdAndStatus(Long payrollId, PayslipStatus status);

    void updateStatusByPayrollId(Long payrollId, PayslipStatus status);
}