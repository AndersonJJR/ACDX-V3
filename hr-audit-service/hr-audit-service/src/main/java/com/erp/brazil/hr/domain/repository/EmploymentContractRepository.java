package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.EmploymentContract;
import com.erp.brazil.hr.domain.model.ContractType;
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
public interface EmploymentContractRepository extends JpaRepository<EmploymentContract, Long> {

    Optional<EmploymentContract> findByEmployeeId(Long employeeId);

    Optional<EmploymentContract> findByUuid(String uuid);

    List<EmploymentContract> findByEmployeeIdIn(List<Long> employeeIds);

    @Query("SELECT c FROM EmploymentContract c WHERE c.employee.companyId = :companyId AND c.contractType = :contractType")
    List<EmploymentContract> findByCompanyIdAndContractType(@Param("companyId") Long companyId,
                                                             @Param("contractType") ContractType contractType);

    @Query("SELECT c FROM EmploymentContract c WHERE c.employee.companyId = :companyId AND c.contractEndDate IS NOT NULL AND " +
           "c.contractEndDate BETWEEN :startDate AND :endDate")
    List<EmploymentContract> findByCompanyIdAndContractEndDateBetween(@Param("companyId") Long companyId,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM EmploymentContract c WHERE c.employee.companyId = :companyId AND c.probationEndDate IS NOT NULL AND " +
           "c.probationEndDate BETWEEN :startDate AND :endDate")
    List<EmploymentContract> findByCompanyIdAndProbationEndDateBetween(@Param("companyId") Long companyId,
                                                                        @Param("startDate") LocalDate startDate,
                                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM EmploymentContract c WHERE c.employee.companyId = :companyId AND c.contractStartDate <= :date AND " +
           "(c.contractEndDate IS NULL OR c.contractEndDate >= :date)")
    List<EmploymentContract> findActiveContractsByCompanyIdAndDate(@Param("companyId") Long companyId,
                                                                    @Param("date") LocalDate date);
}