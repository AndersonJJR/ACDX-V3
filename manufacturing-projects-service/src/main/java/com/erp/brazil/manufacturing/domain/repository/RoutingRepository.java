package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.Routing;
import com.erp.brazil.manufacturing.domain.model.Routing.RoutingStatus;
import com.erp.brazil.manufacturing.domain.model.Routing.RoutingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutingRepository extends JpaRepository<Routing, UUID> {

    List<Routing> findByProductId(UUID productId);

    List<Routing> findByProductIdAndStatus(UUID productId, RoutingStatus status);

    List<Routing> findByType(RoutingType type);

    List<Routing> findByStatus(RoutingStatus status);

    Page<Routing> findByStatus(RoutingStatus status, Pageable pageable);

    Optional<Routing> findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID productId, RoutingStatus status, java.time.LocalDateTime effectiveFrom, java.time.LocalDateTime effectiveTo);

    boolean existsByProductIdAndVersion(UUID productId, String version);
}