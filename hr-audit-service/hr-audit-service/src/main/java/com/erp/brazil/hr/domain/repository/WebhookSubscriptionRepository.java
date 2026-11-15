package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {
    Optional<WebhookSubscription> findByUuid(UUID uuid);
    List<WebhookSubscription> findByCompanyId(Long companyId);
    List<WebhookSubscription> findByCompanyIdAndIsActiveTrue(Long companyId);
    List<WebhookSubscription> findByEventContaining(String event);
    List<WebhookSubscription> findByIsActiveTrue();
    Optional<WebhookSubscription> findByCompanyIdAndName(Long companyId, String name);
    boolean existsByCompanyIdAndUrl(Long companyId, String url);
}