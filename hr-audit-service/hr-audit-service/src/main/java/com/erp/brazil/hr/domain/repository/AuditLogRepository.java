package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    @Query("{ 'entityId': ?0, 'entityType': ?1 }")
    List<AuditLog> findByEntityIdAndEntityType(String entityId, String entityType);

    @Query("{ 'entityId': ?0, 'entityType': ?1 }")
    Page<AuditLog> findByEntityIdAndEntityType(String entityId, String entityType, Pageable pageable);

    @Query("{ 'companyId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<AuditLog> findByCompanyIdAndTimestampBetween(Long companyId, Instant start, Instant end, Pageable pageable);

    @Query("{ 'userId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<AuditLog> findByUserIdAndTimestampBetween(String userId, Instant start, Instant end, Pageable pageable);

    @Query("{ 'action': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<AuditLog> findByActionAndTimestampBetween(String action, Instant start, Instant end, Pageable pageable);

    @Query("{ 'entityType': ?0, 'action': ?1, 'timestamp': { $gte: ?2, $lte: ?3 } }")
    Page<AuditLog> findByEntityTypeAndActionAndTimestampBetween(String entityType, String action, Instant start, Instant end, Pageable pageable);

    @Query("{ 'riskLevel': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<AuditLog> findByRiskLevelAndTimestampBetween(String riskLevel, Instant start, Instant end, Pageable pageable);

    @Query("{ 'complianceTags': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Page<AuditLog> findByComplianceTagAndTimestampBetween(String tag, Instant start, Instant end, Pageable pageable);

    @Query("{ 'timestamp': { $lt: ?0 } }")
    void deleteByTimestampBefore(Instant timestamp);

    long countByCompanyIdAndTimestampBetween(Long companyId, Instant start, Instant end);
}