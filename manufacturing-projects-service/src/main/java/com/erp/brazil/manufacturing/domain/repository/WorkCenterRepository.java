package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkCenter;
import com.erp.brazil.manufacturing.domain.model.WorkCenter.WorkCenterStatus;
import com.erp.brazil.manufacturing.domain.model.WorkCenter.WorkCenterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkCenterRepository extends JpaRepository<WorkCenter, UUID> {

    Optional<WorkCenter> findByCode(String code);

    List<WorkCenter> findByType(WorkCenterType type);

    List<WorkCenter> findByStatus(WorkCenterStatus status);

    List<WorkCenter> findByTypeAndStatus(WorkCenterType type, WorkCenterStatus status);

    Page<WorkCenter> findByStatus(WorkCenterStatus status, Pageable pageable);

    List<WorkCenter> findByCostCenterId(UUID costCenterId);

    List<WorkCenter> findByLocationId(UUID locationId);

    boolean existsByCode(String code);
}