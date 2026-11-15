package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.ItemVariant;
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
public interface ItemVariantRepository extends JpaRepository<ItemVariant, UUID> {

    Optional<ItemVariant> findByCompanyIdAndItemIdAndVariantCode(UUID companyId, UUID itemId, String variantCode);

    List<ItemVariant> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    Page<ItemVariant> findByCompanyIdAndItemId(UUID companyId, UUID itemId, Pageable pageable);

    List<ItemVariant> findByCompanyIdAndIsActiveTrue(UUID companyId);

    Optional<ItemVariant> findByCompanyIdAndBarcode(UUID companyId, String barcode);

    boolean existsByCompanyIdAndItemIdAndVariantCodeAndIdNot(UUID companyId, UUID itemId, String variantCode, UUID id);

    @Query("SELECT iv FROM ItemVariant iv WHERE iv.companyId = :companyId AND iv.itemId = :itemId AND iv.isActive = true ORDER BY iv.variantCode")
    List<ItemVariant> findActiveVariantsByItem(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId);
}