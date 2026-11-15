package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByUuid(UUID uuid);
    Optional<NotificationTemplate> findByCompanyIdAndCodeAndLocale(Long companyId, String code, String locale);
    List<NotificationTemplate> findByCompanyId(Long companyId);
    List<NotificationTemplate> findByCompanyIdAndChannel(Long companyId, NotificationTemplate.Channel channel);
    List<NotificationTemplate> findByChannel(NotificationTemplate.Channel channel);
    List<NotificationTemplate> findByIsActiveTrueAndChannel(NotificationTemplate.Channel channel);
    List<NotificationTemplate> findByIsSystemTrue();
    boolean existsByCompanyIdAndCodeAndLocale(Long companyId, String code, String locale);
}