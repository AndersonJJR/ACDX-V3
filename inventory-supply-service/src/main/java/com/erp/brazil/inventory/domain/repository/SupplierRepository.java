package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Supplier;
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
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Optional<Supplier> findByCompanyIdAndSupplierCode(UUID companyId, String supplierCode);

    Optional<Supplier> findByCompanyIdAndCnpj(UUID companyId, String cnpj);

    Optional<Supplier> findByCompanyIdAndCpf(UUID companyId, String cpf);

    List<Supplier> findByCompanyIdAndIsActiveTrue(UUID companyId);

    Page<Supplier> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);

    List<Supplier> findByCompanyIdAndSupplierType(UUID companyId, Supplier.SupplierType supplierType);

    List<Supplier> findByCompanyIdAndIsApprovedTrue(UUID companyId);

    List<Supplier> findByCompanyIdAndTaxRegime(UUID companyId, Supplier.TaxRegime taxRegime);

    boolean existsByCompanyIdAndSupplierCodeAndIdNot(UUID companyId, String supplierCode, UUID id);

    boolean existsByCompanyIdAndCnpjAndIdNot(UUID companyId, String cnpj, UUID id);

    @Query("SELECT s FROM Supplier s WHERE s.companyId = :companyId AND s.isActive = true AND (s.supplierName LIKE %:search% OR s.supplierCode LIKE %:search% OR s.cnpj LIKE %:search%) ORDER BY s.supplierName")
    Page<Supplier> searchSuppliers(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);
}