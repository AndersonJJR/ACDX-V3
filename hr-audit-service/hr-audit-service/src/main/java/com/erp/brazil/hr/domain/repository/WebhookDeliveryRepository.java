package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    Optional<WebhookDelivery> findByUuid(UUID uuid);
    List<WebhookDelivery> findBySubscriptionId(Long subscriptionId);
    List<WebhookDelivery> findBySubscriptionIdAndStatus(Long subscriptionId, WebhookDelivery.DeliveryStatus status);
    List<WebhookDelivery> findByStatus(WebhookDelivery.DeliveryStatus status);
    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookDelivery.DeliveryStatus status, Instant now);
    List<WebhookDelivery> findByEventType(String eventType);
    long countBySubscriptionIdAndStatus(Long subscriptionId, WebhookDelivery.DeliveryStatus status);

    @Query("SELECT w FROM WebhookDelivery w WHERE w.subscriptionId = :subscriptionId ORDER BY w.attempt DESC")
    List<WebhookDelivery> findBySubscriptionIdOrderByAttemptDesc(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT w FROM WebhookDelivery w WHERE w.status = :status AND w.nextRetryAt IS NOT NULL AND w.nextRetryAt <= :now")
    List<WebhookDelivery> findRetryableDeliveries(@Param("status") WebhookDelivery.DeliveryStatus status, @Param("now") Instant now);
}