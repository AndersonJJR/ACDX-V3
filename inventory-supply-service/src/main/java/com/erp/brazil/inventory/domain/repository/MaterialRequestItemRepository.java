package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.MaterialRequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialRequestItemRepository extends JpaRepository<MaterialRequestItem, UUID> {

    List<MaterialRequestItem> findByMaterialRequestId(UUID materialRequestId);

    Page<MaterialRequestItem> findByMaterialRequestId(UUID materialRequestId, Pageable pageable);

    List<MaterialRequestItem> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.materialRequestId = :mrId AND mri.qty > mri.issuedQty")
    List<MaterialRequestItem> findPendingIssueItems(@Param("mrId") UUID mrId);

    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.materialRequestId = :mrId AND mri.qty > mri.transferredQty")
    List<MaterialRequestItem> findPendingTransferItems(@Param("mrId") UUID mrId);

    @Query("SELECT SUM(mri.qty - mri.issuedQty) FROM MaterialRequestItem mri WHERE mri.materialRequestId = :mrId")
    java.math.BigDecimal getTotalPendingIssueQty(@Param("mrId") UUID mrId);
}