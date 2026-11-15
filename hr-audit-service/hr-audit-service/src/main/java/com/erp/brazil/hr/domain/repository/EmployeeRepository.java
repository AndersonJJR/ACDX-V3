package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUuid(UUID uuid);
    Optional<Employee> findByEmployeeNumberAndCompanyId(String employeeNumber, Long companyId);
    Optional<Employee> findByCpf(String cpf);
    List<Employee> findByCompanyId(Long companyId);
    List<Employee> findByCompanyIdAndStatus(Long companyId, Employee.EmployeeStatus status);
    List<Employee> findByCompanyIdAndHireDateBetween(Long companyId, java.time.LocalDate start, java.time.LocalDate end);
    List<Employee> searchByNameOrCpfOrEmail(Long companyId, String term);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND " +
           "(LOWER(e.fullName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "e.cpf LIKE CONCAT('%', :term, '%') OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "e.employeeNumber LIKE CONCAT('%', :term, '%'))")
    List<Employee> search(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyIdAndStatus(Long companyId, Employee.EmployeeStatus status);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByEmployeeNumberAndCompanyIdAndIdNot(String employeeNumber, Long companyId, Long id);
}