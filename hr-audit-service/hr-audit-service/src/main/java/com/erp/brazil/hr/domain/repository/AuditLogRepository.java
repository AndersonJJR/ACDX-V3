package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByCompanyIdOrderByTimestampDesc(Long companyId);
    List<AuditLog> findByCompanyIdAndTimestampBetween(Long companyId, Instant start, Instant end);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByCompanyIdAndEntityTypeAndTimestampBetween(Long companyId, String entityType, Instant start, Instant end);
    List<AuditLog> findByCorrelationId(String correlationId);
    List<AuditLog> findBySessionId(String sessionId);
    long countByCompanyIdAndTimestampBefore(Long companyId, Instant before);

    @Query("{ 'companyId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<AuditLog> findByCompanyIdAndDateRange(Long companyId, Instant start, Instant end);

    @Query("{ 'companyId': ?0, 'userId': ?1, 'timestamp': { $gte: ?2, $lte: ?3 } }")
    List<AuditLog> findByCompanyIdAndUserIdAndDateRange(Long companyId, Long userId, Instant start, Instant end);

    @Query("{ 'entityType': ?0, 'entityId': ?1 }")
    List<AuditLog> findEntityHistory(String entityType, String entityId);
}