package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.LGPDConsent;
import com.erp.brazil.sales.domain.model.LGPDConsentStatus;
import com.erp.brazil.sales.domain.model.LGPDConsentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LGPDConsentRepository extends JpaRepository<LGPDConsent, UUID> {

    Optional<LGPDConsent> findByEntityTypeAndEntityIdAndType(
            LGPDConsent.EntityType entityType, UUID entityId, LGPDConsentType type);

    List<LGPDConsent> findByEntityTypeAndEntityId(LGPDConsent.EntityType entityType, UUID entityId);

    List<LGPDConsent> findByEntityTypeAndEntityIdAndStatus(
            LGPDConsent.EntityType entityType, UUID entityId, LGPDConsentStatus status);

    @Query("SELECT c FROM LGPDConsent c WHERE c.entityType = :entityType AND c.entityId = :entityId " +
           "AND c.type = :type AND c.status = :status ORDER BY c.grantedAt DESC")
    Optional<LGPDConsent> findLatestByEntityAndTypeAndStatus(
            @Param("entityType") LGPDConsent.EntityType entityType,
            @Param("entityId") UUID entityId,
            @Param("type") LGPDConsentType type,
            @Param("status") LGPDConsentStatus status);

    @Query("SELECT c FROM LGPDConsent c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :now AND c.status = :status")
    List<LGPDConsent> findExpiredConsents(@Param("now") LocalDateTime now, @Param("status") LGPDConsentStatus status);

    Page<LGPDConsent> findByEntityTypeAndEntityId(LGPDConsent.EntityType entityType, UUID entityId, Pageable pageable);

    long countByEntityTypeAndEntityIdAndStatus(LGPDConsent.EntityType entityType, UUID entityId, LGPDConsentStatus status);
}