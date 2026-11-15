package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.WebhookSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {

    Optional<WebhookSubscription> findByUuid(String uuid);

    List<WebhookSubscription> findByCompanyId(Long companyId);

    Page<WebhookSubscription> findByCompanyId(Long companyId, Pageable pageable);

    List<WebhookSubscription> findByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT ws FROM WebhookSubscription ws WHERE ws.companyId = :companyId AND ws.isActive = true AND :event MEMBER OF ws.events")
    List<WebhookSubscription> findByCompanyIdAndEvent(@Param("companyId") Long companyId,
                                                       @Param("event") String event);

    @Query("SELECT ws FROM WebhookSubscription ws WHERE ws.isActive = true AND ws.consecutiveFailures >= :threshold")
    List<WebhookSubscription> findWithHighFailureRate(@Param("threshold") Integer threshold);

    boolean existsByCompanyIdAndUrl(Long companyId, String url);
}