package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.RoutingOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingOperationRepository extends JpaRepository<RoutingOperation, UUID> {

    List<RoutingOperation> findByRoutingIdOrderBySequence(UUID routingId);

    List<RoutingOperation> findByWorkCenterId(UUID workCenterId);

    List<RoutingOperation> findByRoutingIdAndWorkCenterId(UUID routingId, UUID workCenterId);

    boolean existsByRoutingIdAndSequence(UUID routingId, Integer sequence);
}