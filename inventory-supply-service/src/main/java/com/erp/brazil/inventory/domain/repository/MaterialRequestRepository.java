package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.MaterialRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, UUID> {

    Optional<MaterialRequest> findByCompanyIdAndMrNo(UUID companyId, String mrNo);

    List<MaterialRequest> findByCompanyIdAndStatus(UUID companyId, MaterialRequest.MRStatus status);

    Page<MaterialRequest> findByCompanyIdAndStatus(UUID companyId, MaterialRequest.MRStatus status, Pageable pageable);

    List<MaterialRequest> findByCompanyIdAndWarehouseId(UUID companyId, UUID warehouseId);

    List<MaterialRequest> findByCompanyIdAndProductionOrderId(UUID companyId, UUID productionOrderId);

    List<MaterialRequest> findByCompanyIdAndSalesOrderId(UUID companyId, UUID salesOrderId);

    List<MaterialRequest> findByCompanyIdAndRequestDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<MaterialRequest> findByCompanyIdAndStatusIn(UUID companyId, List<MaterialRequest.MRStatus> statuses);

    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.companyId = :companyId AND mr.status IN ('APPROVED', 'PARTIALLY_ISSUED') ORDER BY mr.requiredDate ASC")
    List<MaterialRequest> findOpenMaterialRequests(@Param("companyId") UUID companyId);

    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.companyId = :companyId AND mr.status = 'APPROVED' AND mr.requiredDate < :today")
    List<MaterialRequest> findOverdueMaterialRequests(@Param("companyId") UUID companyId, @Param("today") LocalDate today);

    boolean existsByCompanyIdAndMrNoAndIdNot(UUID companyId, String mrNo, UUID id);

    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.companyId = :companyId AND mr.purpose = :purpose AND mr.status IN ('APPROVED', 'PARTIALLY_ISSUED')")
    List<MaterialRequest> findByPurposeAndOpenStatus(@Param("companyId") UUID companyId, @Param("purpose") MaterialRequest.Purpose purpose);
}