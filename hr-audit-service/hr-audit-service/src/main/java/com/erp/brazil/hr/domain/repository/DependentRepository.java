package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {
    Optional<Dependent> findByUuid(UUID uuid);
    List<Dependent> findByEmployeeId(Long employeeId);
    List<Dependent> findByEmployeeIdAndIsIrrfDependentTrue(Long employeeId);
    List<Dependent> findByEmployeeIdAndIsHealthInsuranceDependentTrue(Long employeeId);
    List<Dependent> findByEmployeeIdAndIsDentalInsuranceDependentTrue(Long employeeId);
    List<Dependent> findByEmployeeIdAndIsPensionDependentTrue(Long employeeId);
    Optional<Dependent> findByCpf(String cpf);
    List<Dependent> findByEmployeeIdAndRelationship(Long employeeId, Dependent.Relationship relationship);
    boolean existsByEmployeeIdAndCpf(Long employeeId, String cpf);
}