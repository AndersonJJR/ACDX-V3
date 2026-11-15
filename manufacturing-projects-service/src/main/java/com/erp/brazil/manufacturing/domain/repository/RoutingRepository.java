package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.Routing;
import com.erp.brazil.manufacturing.domain.model.Routing.RoutingStatus;
import com.erp.brazil.manufacturing.domain.model.Routing.RoutingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutingRepository extends JpaRepository<Routing, UUID> {

    Optional<Routing> findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID productId, RoutingStatus status, LocalDateTime effectiveFrom, LocalDateTime effectiveTo);

    List<Routing> findByProductIdAndStatusOrderByVersionDesc(UUID productId, RoutingStatus status);

    List<Routing> findByProductIdAndTypeAndStatus(UUID productId, RoutingType type, RoutingStatus status);

    Page<Routing> findByStatus(RoutingStatus status, Pageable pageable);

    Page<Routing> findByProductId(UUID productId, Pageable pageable);

    boolean existsByProductIdAndVersion(UUID productId, String version);
}