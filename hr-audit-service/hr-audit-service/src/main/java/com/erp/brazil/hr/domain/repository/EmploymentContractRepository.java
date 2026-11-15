package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.EmploymentContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentContractRepository extends JpaRepository<EmploymentContract, Long> {
    Optional<EmploymentContract> findByUuid(UUID uuid);
    List<EmploymentContract> findByEmployeeId(Long employeeId);
    Optional<EmploymentContract> findActiveByEmployeeIdAndDate(Long employeeId, LocalDate date);

    @Query("SELECT c FROM EmploymentContract c WHERE c.employeeId = :employeeId " +
           "AND c.contractStartDate <= :date AND (c.contractEndDate IS NULL OR c.contractEndDate >= :date)")
    Optional<EmploymentContract> findActiveContract(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    List<EmploymentContract> findByDepartmentId(Long departmentId);
    List<EmploymentContract> findByCostCenterId(Long costCenterId);
    List<EmploymentContract> findByUnionId(Long unionId);
    List<EmploymentContract> findByCollectiveAgreementId(Long collectiveAgreementId);
    boolean existsByEmployeeIdAndContractStartDateLessThanEqualAndContractEndDateGreaterThanEqual(
            Long employeeId, LocalDate startDate, LocalDate endDate);
}