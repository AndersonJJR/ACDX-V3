package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.NotificationTemplate;
import com.erp.brazil.hr.domain.model.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByCompanyIdAndCodeAndLocale(Long companyId, String code, String locale);

    Optional<NotificationTemplate> findByCodeAndLocaleAndCompanyIdIsNull(String code, String locale);

    List<NotificationTemplate> findByCompanyId(Long companyId);

    Page<NotificationTemplate> findByCompanyId(Long companyId, Pageable pageable);

    List<NotificationTemplate> findByCompanyIdAndIsActiveTrue(Long companyId);

    List<NotificationTemplate> findByChannel(NotificationChannel channel);

    List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);

    List<NotificationTemplate> findByCompanyIdAndChannelAndIsActiveTrue(Long companyId, NotificationChannel channel);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE (nt.companyId = :companyId OR nt.companyId IS NULL) AND nt.code = :code AND nt.locale = :locale AND nt.isActive = true ORDER BY nt.companyId DESC")
    Optional<NotificationTemplate> findBestMatch(@Param("companyId") Long companyId,
                                                  @Param("code") String code,
                                                  @Param("locale") String locale);
}