package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.CollectiveAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectiveAgreementRepository extends JpaRepository<CollectiveAgreement, Long> {

    Optional<CollectiveAgreement> findByUnionIdAndCode(Long unionId, String code);

    List<CollectiveAgreement> findByUnionId(Long unionId);

    List<CollectiveAgreement> findByUnionIdAndIsActiveTrue(Long unionId);

    @Query("SELECT ca FROM CollectiveAgreement ca WHERE ca.unionId = :unionId AND ca.isActive = true AND ca.startDate <= :date AND (ca.endDate IS NULL OR ca.endDate >= :date)")
    Optional<CollectiveAgreement> findActiveByUnionIdAndDate(@Param("unionId") Long unionId,
                                                              @Param("date") LocalDate date);

    @Query("SELECT ca FROM CollectiveAgreement ca WHERE ca.isActive = true AND ca.startDate <= :date AND (ca.endDate IS NULL OR ca.endDate >= :date)")
    List<CollectiveAgreement> findAllActiveByDate(@Param("date") LocalDate date);
}