package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Customer;
import com.erp.brazil.sales.domain.model.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCustomerCodeAndCompanyId(String customerCode, UUID companyId);

    Optional<Customer> findByDocumentNumber(String documentNumber);

    List<Customer> findByCompanyIdAndCustomerType(UUID companyId, CustomerType customerType);

    List<Customer> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<Customer> findByTerritoryId(UUID territoryId);

    List<Customer> findBySalesPersonId(UUID salesPersonId);

    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId AND c.isActive = true AND c.isBlocked = false ORDER BY c.tradeName")
    List<Customer> findActiveAndUnblockedByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId AND (c.tradeName LIKE :pattern OR c.legalName LIKE :pattern OR c.documentNumber LIKE :pattern) ORDER BY c.tradeName")
    List<Customer> searchByCompanyId(@Param("companyId") UUID companyId, @Param("pattern") String pattern);

    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId ORDER BY c.tradeName")
    Page<Customer> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    boolean existsByCustomerCodeAndCompanyIdAndIdNot(String customerCode, UUID companyId, UUID id);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);
}