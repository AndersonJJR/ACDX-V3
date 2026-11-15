package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.EmployeeBenefit;
import com.erp.brazil.hr.domain.model.EmployeeBenefitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefit, Long> {

    List<EmployeeBenefit> findByEmployeeId(Long employeeId);

    List<EmployeeBenefit> findByEmployeeIdAndStatus(Long employeeId, EmployeeBenefitStatus status);

    List<EmployeeBenefit> findByBenefitId(Long benefitId);

    @Query("SELECT eb FROM EmployeeBenefit eb WHERE eb.employee.companyId = :companyId AND eb.status = :status")
    List<EmployeeBenefit> findByCompanyIdAndStatus(@Param("companyId") Long companyId,
                                                    @Param("status") EmployeeBenefitStatus status);

    @Query("SELECT eb FROM EmployeeBenefit eb WHERE eb.employeeId = :employeeId AND eb.status = :status AND eb.startDate <= :date AND (eb.endDate IS NULL OR eb.endDate >= :date)")
    List<EmployeeBenefit> findActiveByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                                         @Param("status") EmployeeBenefitStatus status,
                                                         @Param("date") LocalDate date);

    @Query("SELECT eb FROM EmployeeBenefit eb WHERE eb.benefitId = :benefitId AND eb.status = :status AND eb.startDate <= :date AND (eb.endDate IS NULL OR eb.endDate >= :date)")
    List<EmployeeBenefit> findActiveByBenefitIdAndDate(@Param("benefitId") Long benefitId,
                                                        @Param("status") EmployeeBenefitStatus status,
                                                        @Param("date") LocalDate date);

    void deleteByEmployeeId(Long employeeId);
}