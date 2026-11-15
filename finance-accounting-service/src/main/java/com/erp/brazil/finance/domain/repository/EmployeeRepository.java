package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByCode(String code);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByActiveTrueOrderByName();

    Page<Employee> findByActiveTrue(Pageable pageable);

    Page<Employee> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}