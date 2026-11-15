package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.WebhookDelivery;
import com.erp.brazil.hr.domain.model.WebhookDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findBySubscriptionId(Long subscriptionId);

    Page<WebhookDelivery> findBySubscriptionId(Long subscriptionId, Pageable pageable);

    List<WebhookDelivery> findBySubscriptionIdAndStatus(Long subscriptionId, WebhookDeliveryStatus status);

    List<WebhookDelivery> findByStatus(WebhookDeliveryStatus status);

    Page<WebhookDelivery> findByStatus(WebhookDeliveryStatus status, Pageable pageable);

    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.status = :status AND wd.nextRetryAt IS NOT NULL AND wd.nextRetryAt <= :now")
    List<WebhookDelivery> findReadyForRetry(@Param("status") WebhookDeliveryStatus status,
                                             @Param("now") Instant now);

    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.subscriptionId = :subscriptionId AND wd.status IN :statuses")
    List<WebhookDelivery> findBySubscriptionIdAndStatusIn(@Param("subscriptionId") Long subscriptionId,
                                                           @Param("statuses") List<WebhookDeliveryStatus> statuses);

    long countBySubscriptionIdAndStatus(Long subscriptionId, WebhookDeliveryStatus status);

    void deleteBySubscriptionId(Long subscriptionId);

    void deleteByStatusAndCreatedAtBefore(WebhookDeliveryStatus status, Instant before);
}