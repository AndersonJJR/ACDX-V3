package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Optional<Supplier> findByCompanyIdAndSupplierCode(UUID companyId, String supplierCode);

    Optional<Supplier> findByCnpj(String cnpj);

    List<Supplier> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<Supplier> findByCompanyIdAndIsApprovedTrueAndIsActiveTrue(UUID companyId);

    List<Supplier> findByCompanyIdAndSupplierType(UUID companyId, Supplier.SupplierType supplierType);

    boolean existsByCompanyIdAndSupplierCodeAndIdNot(UUID companyId, String supplierCode, UUID id);

    boolean existsByCnpjAndIdNot(String cnpj, UUID id);
}