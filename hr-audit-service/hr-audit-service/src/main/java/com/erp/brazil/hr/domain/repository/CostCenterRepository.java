package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.CostCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {

    Optional<CostCenter> findByCompanyIdAndCode(Long companyId, String code);

    List<CostCenter> findByCompanyId(Long companyId);

    Page<CostCenter> findByCompanyId(Long companyId, Pageable pageable);

    List<CostCenter> findByCompanyIdAndIsActiveTrue(Long companyId);

    List<CostCenter> findByParentId(Long parentId);

    List<CostCenter> findByCompanyIdAndParentIdIsNull(Long companyId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.companyId = :companyId AND cc.isActive = true")
    List<CostCenter> findActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.responsibleId = :responsibleId")
    List<CostCenter> findByResponsibleId(@Param("responsibleId") Long responsibleId);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}