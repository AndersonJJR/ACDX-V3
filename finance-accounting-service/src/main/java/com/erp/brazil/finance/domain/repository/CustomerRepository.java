package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Customer;
import com.erp.brazil.finance.domain.model.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCode(String code);

    Optional<Customer> findByDocumentNumber(String documentNumber);

    List<Customer> findByActiveTrueOrderByName();

    List<Customer> findByStatus(CustomerStatus status);

    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Customer c WHERE c.name ILIKE %:name% OR c.tradeName ILIKE %:name% OR c.documentNumber LIKE %:name%")
    Page<Customer> searchByNameOrDocument(@Param("name") String name, Pageable pageable);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    Page<Customer> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);
}