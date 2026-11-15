package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByUuid(UUID uuid);
    List<Notification> findByCompanyId(Long companyId);
    List<Notification> findByRecipientTypeAndRecipientId(Notification.RecipientType type, Long recipientId);
    List<Notification> findByRecipientEmail(String email);
    List<Notification> findByStatus(Notification.Status status);
    List<Notification> findByTemplateId(Long templateId);
    List<Notification> findByScheduledAtBeforeAndStatusIn(Instant now, List<Notification.Status> statuses);
    List<Notification> findByCompanyIdAndStatus(Long companyId, Notification.Status status);
    List<Notification> findByExternalId(String externalId);

    @Query("SELECT n FROM Notification n WHERE n.recipientType = :type AND n.recipientId = :recipientId AND n.status IN (:statuses) ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientAndStatuses(@Param("type") Notification.RecipientType type,
                                                   @Param("recipientId") Long recipientId,
                                                   @Param("statuses") List<Notification.Status> statuses);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt IS NOT NULL AND n.scheduledAt <= :now")
    List<Notification> findScheduledForSending(@Param("status") Notification.Status status, @Param("now") Instant now);

    long countByRecipientTypeAndRecipientIdAndStatus(Notification.RecipientType type, Long recipientId, Notification.Status status);
}