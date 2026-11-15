package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkStation;
import com.erp.brazil.manufacturing.domain.model.WorkStation.WorkStationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, UUID> {

    Optional<WorkStation> findByCode(String code);

    List<WorkStation> findByWorkCenterId(UUID workCenterId);

    List<WorkStation> findByWorkCenterIdAndStatus(UUID workCenterId, WorkStationStatus status);

    List<WorkStation> findByStatus(WorkStationStatus status);

    Page<WorkStation> findByWorkCenterId(UUID workCenterId, Pageable pageable);

    boolean existsByCode(String code);
}