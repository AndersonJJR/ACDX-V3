package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Dependent;
import com.erp.brazil.hr.domain.model.DependentRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByEmployeeId(Long employeeId);

    List<Dependent> findByEmployeeIdAndRelationship(Long employeeId, DependentRelationship relationship);

    Optional<Dependent> findByEmployeeIdAndCpf(Long employeeId, String cpf);

    @Query("SELECT d FROM Dependent d WHERE d.employee.companyId = :companyId AND d.isIrrfDependent = true")
    List<Dependent> findIrrfDependentsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT d FROM Dependent d WHERE d.employee.companyId = :companyId AND d.isHealthInsuranceDependent = true")
    List<Dependent> findHealthDependentsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT d FROM Dependent d WHERE d.employeeId = :employeeId AND d.isIrrfDependent = true")
    List<Dependent> findIrrfDependentsByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM Dependent d WHERE d.employeeId = :employeeId AND d.isHealthInsuranceDependent = true")
    List<Dependent> findHealthDependentsByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT d FROM Dependent d WHERE d.employeeId = :employeeId AND d.studentSince IS NOT NULL AND d.studentUntil IS NOT NULL AND d.studentUntil >= :date")
    List<Dependent> findStudentDependentsByEmployeeId(@Param("employeeId") Long employeeId,
                                                       @Param("date") java.time.LocalDate date);
}