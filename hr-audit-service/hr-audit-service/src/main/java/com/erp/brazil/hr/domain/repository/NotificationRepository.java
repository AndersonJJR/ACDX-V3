package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Notification;
import com.erp.brazil.hr.domain.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientTypeAndRecipientId(String recipientType, Long recipientId);

    Page<Notification> findByRecipientTypeAndRecipientId(String recipientType, Long recipientId, Pageable pageable);

    List<Notification> findByStatus(NotificationStatus status);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    List<Notification> findByCompanyIdAndStatus(Long companyId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.scheduledAt IS NOT NULL AND n.scheduledAt <= :now AND n.status = :status")
    List<Notification> findScheduledNotifications(@Param("now") Instant now,
                                                   @Param("status") NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries AND (n.failedAt IS NULL OR n.failedAt < :since)")
    List<Notification> findFailedNotificationsForRetry(@Param("status") NotificationStatus status,
                                                        @Param("since") Instant since);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :newStatus WHERE n.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("newStatus") NotificationStatus newStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :status, n.failedAt = :now, n.failureReason = :reason WHERE n.id = :id")
    void markAsFailed(@Param("id") Long id, @Param("status") NotificationStatus status,
                       @Param("now") Instant now, @Param("reason") String reason);

    long countByRecipientTypeAndRecipientIdAndStatus(String recipientType, Long recipientId, NotificationStatus status);

    void deleteByCompanyIdAndStatusAndCreatedAtBefore(Long companyId, NotificationStatus status, Instant before);
}