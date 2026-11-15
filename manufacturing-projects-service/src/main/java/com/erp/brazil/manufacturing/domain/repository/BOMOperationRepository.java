package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.BOMOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BOMOperationRepository extends JpaRepository<BOMOperation, UUID> {

    List<BOMOperation> findByBomIdOrderBySequence(UUID bomId);

    List<BOMOperation> findByWorkCenterId(UUID workCenterId);

    List<BOMOperation> findByBomIdAndWorkCenterId(UUID bomId, UUID workCenterId);
}