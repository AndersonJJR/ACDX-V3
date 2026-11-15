package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Employee;
import com.erp.brazil.hr.domain.model.EmployeeStatus;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByCpf(String cpf);

    Optional<Employee> findByEmployeeNumberAndCompanyId(String employeeNumber, Long companyId);

    Optional<Employee> findByUuid(String uuid);

    List<Employee> findByCompanyId(Long companyId);

    Page<Employee> findByCompanyId(Long companyId, Pageable pageable);

    List<Employee> findByCompanyIdAndStatus(Long companyId, EmployeeStatus status);

    Page<Employee> findByCompanyIdAndStatus(Long companyId, EmployeeStatus status, Pageable pageable);

    List<Employee> findByCompanyIdAndStatusIn(Long companyId, List<EmployeeStatus> statuses);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.hireDate BETWEEN :startDate AND :endDate")
    List<Employee> findByCompanyIdAndHireDateBetween(@Param("companyId") Long companyId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.terminationDate BETWEEN :startDate AND :endDate")
    List<Employee> findByCompanyIdAndTerminationDateBetween(@Param("companyId") Long companyId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.birthDate IS NOT NULL AND " +
           "EXTRACT(MONTH FROM e.birthDate) = :month AND EXTRACT(DAY FROM e.birthDate) = :day")
    List<Employee> findBirthdaysByCompanyAndDay(@Param("companyId") Long companyId,
                                                 @Param("month") int month,
                                                 @Param("day") int day);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.contractEndDate IS NOT NULL AND " +
           "e.contractEndDate BETWEEN :startDate AND :endDate")
    List<Employee> findContractsExpiringBetween(@Param("companyId") Long companyId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.probationEndDate IS NOT NULL AND " +
           "e.probationEndDate BETWEEN :startDate AND :endDate")
    List<Employee> findProbationEndingBetween(@Param("companyId") Long companyId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND e.status = 'ACTIVE' AND " +
           "e.employmentContract.leaveBalanceAccrualDate IS NOT NULL AND " +
           "e.employmentContract.leaveBalanceAccrualDate <= :date")
    List<Employee> findEmployeesWithVacationExpiring(@Param("companyId") Long companyId,
                                                      @Param("date") LocalDate date);

    long countByCompanyIdAndStatus(Long companyId, EmployeeStatus status);

    boolean existsByCpfAndCompanyId(String cpf, Long companyId);

    boolean existsByEmployeeNumberAndCompanyId(String employeeNumber, Long companyId);
}