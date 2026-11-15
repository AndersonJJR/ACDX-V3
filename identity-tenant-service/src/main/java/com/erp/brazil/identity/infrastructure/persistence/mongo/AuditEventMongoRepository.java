package com.erp.brazil.identity.infrastructure.persistence.mongo;

import com.erp.brazil.common.domain.event.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * MongoDB Repository for AuditEvent.
 * Stores audit trail in MongoDB for better performance and querying.
 */
@Repository
public interface AuditEventMongoRepository extends MongoRepository<AuditEvent, String> {

    @Query("{ 'tenantId': ?0, 'entityType': ?1, 'entityId': ?2 }")
    List<AuditEvent> findByTenantIdAndEntityTypeAndEntityId(String tenantId, String entityType, String entityId);

    @Query("{ 'tenantId': ?0, 'entityType': ?1 }")
    Page<AuditEvent> findByTenantIdAndEntityType(String tenantId, String entityType, Pageable pageable);

    @Query("{ 'tenantId': ?0, 'changedBy': ?1 }")
    Page<AuditEvent> findByTenantIdAndChangedBy(String tenantId, String changedBy, Pageable pageable);

    @Query("{ 'tenantId': ?0, 'action': ?1 }")
    Page<AuditEvent> findByTenantIdAndAction(String tenantId, AuditEvent.AuditAction action, Pageable pageable);

    @Query("{ 'tenantId': ?0, 'changedAt': { $gte: ?1, $lte: ?2 } }")
    Page<AuditEvent> findByTenantIdAndDateRange(String tenantId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    @Query("{ 'tenantId': ?0 }")
    Page<AuditEvent> findByTenantId(String tenantId, Pageable pageable);

    @Query("{ 'correlationId': ?0 }")
    List<AuditEvent> findByCorrelationId(String correlationId);
}